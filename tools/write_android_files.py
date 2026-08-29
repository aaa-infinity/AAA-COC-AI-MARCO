import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

acc_service = """package com.cocai.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.random.Random

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i("AutoClickService", "Accessibility Service Connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.w("AutoClickService", "Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun performTap(x: Float, y: Float, durationMs: Long = 50L, jitter: Boolean = true, callback: (() -> Unit)? = null) {
        val jitterX = if (jitter) x + Random.nextDouble(-4.0, 4.0).toFloat() else x
        val jitterY = if (jitter) y + Random.nextDouble(-4.0, 4.0).toFloat() else y

        val path = Path().apply {
            moveTo(jitterX, jitterY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }

    fun performBezierSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300L,
        callback: (() -> Unit)? = null
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            val controlX = (startX + endX) / 2 + Random.nextInt(-30, 30)
            val controlY = (startY + endY) / 2 + Random.nextInt(-30, 30)
            quadTo(controlX.toFloat(), controlY.toFloat(), endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }

    fun performMultiTouchTaps(points: List<Pair<Float, Float>>, durationMs: Long = 60L) {
        val builder = GestureDescription.Builder()
        for (pt in points.take(10)) {
            val path = Path().apply {
                val jX = pt.first + Random.nextDouble(-3.0, 3.0).toFloat()
                val jY = pt.second + Random.nextDouble(-3.0, 3.0).toFloat()
                moveTo(jX, jY)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }
        dispatchGesture(builder.build(), null, null)
    }
}
"""

tmpl_matcher = """package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

data class MatchResult(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val confidence: Float
) {
    val centerX: Int get() = x + width / 2
    val centerY: Int get() = y + height / 2
}

class FastTemplateMatcher {

    fun findTemplate(
        screen: Bitmap,
        template: Bitmap,
        threshold: Float = 0.85f,
        step: Int = 4
    ): MatchResult? {
        val screenW = screen.width
        val screenH = screen.height
        val tmplW = template.width
        val tmplH = template.height

        if (tmplW > screenW || tmplH > screenH) return null

        var bestScore = 0.0f
        var bestX = 0
        var bestY = 0

        for (y in 0 until (screenH - tmplH) step step) {
            for (x in 0 until (screenW - tmplW) step step) {
                val score = compareRegion(screen, template, x, y, tmplW, tmplH)
                if (score > bestScore) {
                    bestScore = score
                    bestX = x
                    bestY = y
                    if (score >= 0.95f) {
                        return MatchResult(bestX, bestY, tmplW, tmplH, bestScore)
                    }
                }
            }
        }

        return if (bestScore >= threshold) {
            MatchResult(bestX, bestY, tmplW, tmplH, bestScore)
        } else {
            null
        }
    }

    private fun compareRegion(screen: Bitmap, tmpl: Bitmap, startX: Int, startY: Int, w: Int, h: Int): Float {
        var totalDiff = 0L
        val sampleStep = 3
        var sampledPixels = 0

        for (ty in 0 until h step sampleStep) {
            for (tx in 0 until w step sampleStep) {
                val screenPixel = screen.getPixel(startX + tx, startY + ty)
                val tmplPixel = tmpl.getPixel(tx, ty)

                val rDiff = abs(Color.red(screenPixel) - Color.red(tmplPixel))
                val gDiff = abs(Color.green(screenPixel) - Color.green(tmplPixel))
                val bDiff = abs(Color.blue(screenPixel) - Color.blue(tmplPixel))

                totalDiff += (rDiff + gDiff + bDiff)
                sampledPixels++
            }
        }

        val maxPossibleDiff = sampledPixels * 255L * 3L
        return 1.0f - (totalDiff.toFloat() / maxPossibleDiff.toFloat())
    }
}
"""

coc_engine = """package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

enum class CocStrategy {
    SNEAKY_GOBLIN,
    ROOT_RIDER_SPAM,
    BARCH_WAVE,
    BUILDER_BASE_FARM,
    ORE_STAR_BONUS
}

class CocFarmingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRunning: Boolean = false
        private set

    var currentStrategy: CocStrategy = CocStrategy.SNEAKY_GOBLIN
    var goldCollected: Long = 0
    var elixirCollected: Long = 0
    var oresCollected: Long = 0
    var raidsCompleted: Int = 0

    fun startEngine(strategy: CocStrategy) {
        currentStrategy = strategy
        isRunning = true
        Log.i("CocEngine", "Starting engine with strategy: " + strategy.name)
        scheduleNextStep(1000L) {
            runVillageLoop()
        }
    }

    fun stopEngine() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        Log.i("CocEngine", "Engine stopped.")
    }

    private fun scheduleNextStep(delayMs: Long, action: () -> Unit) {
        if (!isRunning) return
        val randomizedDelay = delayMs + Random.nextLong(100L, 400L)
        handler.postDelayed({
            if (isRunning) action()
        }, randomizedDelay)
    }

    private fun runVillageLoop() {
        Log.i("CocEngine", "Running Village Collection & Maintenance...")
        collectVillageResources {
            trainPresetArmy {
                when (currentStrategy) {
                    CocStrategy.BUILDER_BASE_FARM -> executeBuilderBaseFastFarm()
                    CocStrategy.ORE_STAR_BONUS,
                    CocStrategy.SNEAKY_GOBLIN,
                    CocStrategy.ROOT_RIDER_SPAM,
                    CocStrategy.BARCH_WAVE -> executeMultiplayerRaid()
                }
            }
        }
    }

    private fun collectVillageResources(onComplete: () -> Unit) {
        val tapPoints = listOf(
            Pair(750f, 450f),
            Pair(950f, 520f),
            Pair(1150f, 480f),
            Pair(850f, 650f),
            Pair(1050f, 680f),
            Pair(1600f, 900f)
        )

        var idx = 0
        fun tapNext() {
            if (idx < tapPoints.size && isRunning) {
                val pt = tapPoints[idx++]
                accessibilityService.performTap(pt.first, pt.second) {
                    scheduleNextStep(400L) { tapNext() }
                }
            } else {
                onComplete()
            }
        }
        tapNext()
    }

    private fun trainPresetArmy(onComplete: () -> Unit) {
        accessibilityService.performTap(90f, 830f) {
            scheduleNextStep(1200L) {
                accessibilityService.performTap(1350f, 150f) {
                    scheduleNextStep(800L) {
                        accessibilityService.performTap(1580f, 380f) {
                            scheduleNextStep(800L) {
                                accessibilityService.performTap(1820f, 85f) {
                                    scheduleNextStep(1000L, onComplete)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun executeBuilderBaseFastFarm() {
        Log.i("CocEngine", "Executing Builder Base 2.0 Fast Farming...")
        accessibilityService.performTap(120f, 950f) {
            scheduleNextStep(1500L) {
                accessibilityService.performTap(1400f, 600f) {
                    scheduleNextStep(4000L) {
                        deployAllTroopsPerimeter {
                            surrenderAndReturnHome()
                        }
                    }
                }
            }
        }
    }

    private fun executeMultiplayerRaid() {
        Log.i("CocEngine", "Executing Multiplayer Raid...")
        accessibilityService.performTap(120f, 950f) {
            scheduleNextStep(1500L) {
                accessibilityService.performTap(1450f, 650f) {
                    scheduleNextStep(6000L) {
                        when (currentStrategy) {
                            CocStrategy.SNEAKY_GOBLIN -> deploySneakyGoblins()
                            CocStrategy.ROOT_RIDER_SPAM -> deployRootRiderSmash()
                            else -> deployPerimeterWaves()
                        }
                    }
                }
            }
        }
    }

    private fun deploySneakyGoblins() {
        Log.i("CocEngine", "Deploying Sneaky Goblins on collectors...")
        val dropSpots = listOf(
            Pair(400f, 300f), Pair(600f, 200f), Pair(960f, 150f),
            Pair(1300f, 200f), Pair(1500f, 300f), Pair(1600f, 600f),
            Pair(1400f, 800f), Pair(960f, 900f), Pair(500f, 800f)
        )

        accessibilityService.performTap(200f, 980f) {
            scheduleNextStep(400L) {
                accessibilityService.performMultiTouchTaps(dropSpots)
                scheduleNextStep(3000L) {
                    accessibilityService.performTap(620f, 980f) {
                        scheduleNextStep(300L) {
                            accessibilityService.performTap(960f, 540f)
                            scheduleNextStep(5000L) {
                                triggerHeroAbilities {
                                    scheduleNextStep(20000L) {
                                        surrenderAndReturnHome()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deployRootRiderSmash() {
        Log.i("CocEngine", "Deploying Root Rider + Hero Equipment smash...")
        val entryLine = listOf(Pair(600f, 750f), Pair(700f, 780f), Pair(800f, 800f), Pair(900f, 820f))

        accessibilityService.performTap(200f, 980f) {
            scheduleNextStep(300L) {
                accessibilityService.performMultiTouchTaps(entryLine)
                scheduleNextStep(1000L) {
                    accessibilityService.performTap(300f, 980f)
                    accessibilityService.performTap(750f, 780f)
                    accessibilityService.performTap(400f, 980f)
                    accessibilityService.performTap(800f, 780f)

                    scheduleNextStep(12000L) {
                        triggerHeroAbilities {
                            scheduleNextStep(35000L) {
                                surrenderAndReturnHome()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deployAllTroopsPerimeter(onComplete: () -> Unit) {
        val spots = listOf(Pair(500f, 400f), Pair(960f, 250f), Pair(1400f, 400f), Pair(960f, 800f))
        accessibilityService.performTap(200f, 980f)
        accessibilityService.performMultiTouchTaps(spots)
        scheduleNextStep(2000L, onComplete)
    }

    private fun deployPerimeterWaves() {
        val perimeter = listOf(Pair(300f, 450f), Pair(960f, 150f), Pair(1600f, 450f), Pair(960f, 850f))
        accessibilityService.performTap(200f, 980f)
        accessibilityService.performMultiTouchTaps(perimeter)
        scheduleNextStep(2000L) {
            accessibilityService.performTap(300f, 980f)
            accessibilityService.performMultiTouchTaps(perimeter)
            scheduleNextStep(25000L) {
                surrenderAndReturnHome()
            }
        }
    }

    private fun triggerHeroAbilities(onComplete: () -> Unit) {
        Log.i("CocEngine", "Activating Hero Equipment abilities...")
        accessibilityService.performTap(300f, 980f)
        accessibilityService.performTap(400f, 980f)
        accessibilityService.performTap(500f, 980f)
        accessibilityService.performTap(600f, 980f)
        scheduleNextStep(1000L, onComplete)
    }

    private fun surrenderAndReturnHome() {
        Log.i("CocEngine", "Ending battle and returning home...")
        accessibilityService.performTap(120f, 880f) {
            scheduleNextStep(1000L) {
                accessibilityService.performTap(1100f, 680f) {
                    scheduleNextStep(2500L) {
                        accessibilityService.performTap(960f, 920f) {
                            raidsCompleted++
                            scheduleNextStep(5000L) {
                                runVillageLoop()
                            }
                        }
                    }
                }
            }
        }
    }
}
"""

floating_service = """package com.cocai.autoclicker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.CocFarmingEngine
import com.cocai.autoclicker.engine.CocStrategy

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var farmingEngine: CocFarmingEngine? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val accService = AutoClickAccessibilityService.instance
        if (accService != null) {
            farmingEngine = CocFarmingEngine(accService)
        }

        setupFloatingView()
    }

    private fun setupFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_hud, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(floatingView, params)

        val btnToggle = floatingView?.findViewById<Button>(R.id.btn_toggle_play)
        val btnStrategy = floatingView?.findViewById<Button>(R.id.btn_strategy)
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)

        var selectedStrategy = CocStrategy.SNEAKY_GOBLIN

        btnStrategy?.setOnClickListener {
            selectedStrategy = when (selectedStrategy) {
                CocStrategy.SNEAKY_GOBLIN -> CocStrategy.ROOT_RIDER_SPAM
                CocStrategy.ROOT_RIDER_SPAM -> CocStrategy.BUILDER_BASE_FARM
                CocStrategy.BUILDER_BASE_FARM -> CocStrategy.ORE_STAR_BONUS
                CocStrategy.ORE_STAR_BONUS -> CocStrategy.BARCH_WAVE
                CocStrategy.BARCH_WAVE -> CocStrategy.SNEAKY_GOBLIN
            }
            btnStrategy.text = selectedStrategy.name.replace("_", " ")
        }

        btnToggle?.setOnClickListener {
            val engine = farmingEngine
            if (engine != null) {
                if (engine.isRunning) {
                    engine.stopEngine()
                    btnToggle.text = "▶ START"
                    tvStatus?.text = "Status: IDLE"
                } else {
                    engine.startEngine(selectedStrategy)
                    btnToggle.text = "⏸ PAUSE"
                    tvStatus?.text = "Status: RUNNING (" + selectedStrategy.name + ")"
                }
            } else {
                tvStatus?.text = "Error: Enable Accessibility Service"
            }
        }

        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        farmingEngine?.stopEngine()
        if (floatingView != null) {
            windowManager.removeView(floatingView)
        }
    }
}
"""

main_activity = """package com.cocai.autoclicker.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.cocai.autoclicker.R
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import com.cocai.autoclicker.service.FloatingOverlayService

class MainActivity : Activity() {

    private lateinit var tvAccStatus: TextView
    private lateinit var tvOverlayStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccStatus = findViewById(R.id.tv_accessibility_status)
        tvOverlayStatus = findViewById(R.id.tv_overlay_status)

        findViewById<Button>(R.id.btn_grant_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btn_grant_overlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName)
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Overlay permission already granted!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_start_floating_hud).setOnClickListener {
            if (!AutoClickAccessibilityService.isServiceRunning) {
                Toast.makeText(this, "Please enable Accessibility Service first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant Overlay Permission!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            startService(Intent(this, FloatingOverlayService::class.java))
            Toast.makeText(this, "Floating HUD started! Open Clash of Clans.", Toast.LENGTH_SHORT).show()
            moveTaskToBack(true)
        }
    }

    override fun onResume() {
        super.onResume()
        tvAccStatus.text = if (AutoClickAccessibilityService.isServiceRunning) {
            "Accessibility Service: ENABLED (Ready)"
        } else {
            "Accessibility Service: DISABLED (Action Required)"
        }

        tvOverlayStatus.text = if (Settings.canDrawOverlays(this)) {
            "Overlay Permission: GRANTED"
        } else {
            "Overlay Permission: MISSING"
        }
    }
}
"""

files_to_write = {
    f'{pkg_dir}/service/AutoClickAccessibilityService.kt': acc_service,
    f'{pkg_dir}/engine/TemplateMatcher.kt': tmpl_matcher,
    f'{pkg_dir}/engine/CocFarmingEngine.kt': coc_engine,
    f'{pkg_dir}/service/FloatingOverlayService.kt': floating_service,
    f'{pkg_dir}/ui/MainActivity.kt': main_activity
}

for path, content in files_to_write.items():
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        f.write(content)
    print(f'Created {path}')
