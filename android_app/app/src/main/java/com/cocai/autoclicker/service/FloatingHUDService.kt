package com.cocai.autoclicker.service

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.app.NotificationCompat
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.ClashAutomationCore
import com.cocai.autoclicker.ui.MainActivity
import kotlin.math.abs

/**
 * 👑 Macrorify 8-Button Floating HUD & 24/7 Foreground Service
 */
class FloatingHUDService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingRootView: View? = null
    private var blackScreenView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var automationCore: ClashAutomationCore? = null
    private lateinit var prefs: SharedPreferences

    private var isExpanded: Boolean = true
    private var isViewAttached: Boolean = false
    private var isBlackScreenActive: Boolean = false
    private var screenWidth: Int = 1080
    private var screenHeight: Int = 2400

    private val CHANNEL_ID = "ai_marco_foreground_channel"
    private val NOTIFICATION_ID = 1001

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            startForegroundServiceNotification()
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            prefs = getSharedPreferences("macrorify_hud_prefs", Context.MODE_PRIVATE)

            fetchScreenDimensions()
            initCore()
            setupFloatingLayout()
        } catch (e: Exception) {
            Log.e("FloatingHUD", "Error during onCreate: ${e.message}", e)
            stopSelf()
        }
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ai Marco coc - 24/7 Automation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Clash of Clans auto-farming active in background"
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notifyIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("👑 Ai Marco coc - Active")
            .setContentText("Continuous Loot Farming & Vision AI Active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun fetchScreenDimensions() {
        try {
            val windowMgr = windowManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val metrics = windowMgr.currentWindowMetrics
                screenWidth = metrics.bounds.width()
                screenHeight = metrics.bounds.height()
            } else {
                @Suppress("DEPRECATION")
                val display = windowMgr.defaultDisplay
                val size = Point()
                @Suppress("DEPRECATION")
                display.getSize(size)
                screenWidth = size.x
                screenHeight = size.y
            }
        } catch (e: Exception) {
            screenWidth = 1080
            screenHeight = 2400
        }
    }

    private fun initCore() {
        try {
            val acc = AutoClickAccessibilityService.instance
            if (acc != null) {
                automationCore = ClashAutomationCore(this, acc)

                automationCore?.onStatusUpdate = { newStatus ->
                    floatingRootView?.post {
                        try {
                            val tvStatus = floatingRootView?.findViewById<TextView>(R.id.tv_hud_status_badge)
                            val shortStatus = when {
                                newStatus.contains("STARTING", true) -> "START"
                                newStatus.contains("WALL", true) -> "WALLS"
                                newStatus.contains("HARVEST", true) -> "LOOT"
                                newStatus.contains("SEARCHING", true) || newStatus.contains("INSPECTING", true) -> "SEARCH"
                                newStatus.contains("RAIDING", true) -> "RAID"
                                newStatus.contains("VICTORY", true) -> "WIN"
                                newStatus.contains("STOPPED", true) -> "IDLE"
                                else -> "ACTIVE"
                            }
                            tvStatus?.text = shortStatus
                        } catch (e: Exception) {
                            Log.w("FloatingHUD", "Error updating status: ${e.message}")
                        }
                    }
                }

                acc.emergencyStopListener = {
                    stopMacroExecution()
                    Toast.makeText(this, "🚨 [PANIC STOP ACTIVATED] Macro Halted via Volume Down!", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("FloatingHUD", "Error initializing core: ${e.message}")
        }
    }

    private fun stopMacroExecution() {
        try {
            automationCore?.stopLoop()
            val tvStatus = floatingRootView?.findViewById<TextView>(R.id.tv_hud_status_badge)
            val btnPlayPause = floatingRootView?.findViewById<Button>(R.id.btn_hud_play_pause)
            tvStatus?.text = "IDLE"
            btnPlayPause?.text = "▶"
        } catch (e: Exception) {
            Log.w("FloatingHUD", "Error stopping macro: ${e.message}")
        }
    }

    private fun setupFloatingLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e("FloatingHUD", "Cannot draw overlays: permission not granted!")
            stopSelf()
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val savedX = prefs.getInt("pos_x", screenWidth - 250)
        val savedY = prefs.getInt("pos_y", 150)
        isExpanded = prefs.getBoolean("is_expanded", true)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = savedX.coerceIn(0, screenWidth - 100)
        params.y = savedY.coerceIn(0, screenHeight - 100)
        windowParams = params

        try {
            val inflater = LayoutInflater.from(this)
            floatingRootView = inflater.inflate(R.layout.layout_macrorify_hud, null)
            windowManager?.addView(floatingRootView, params)
            isViewAttached = true
        } catch (e: Exception) {
            Log.e("FloatingHUD", "Failed to add floating view to WindowManager: ${e.message}")
            stopSelf()
            return
        }

        bindControls()
    }

    private fun bindControls() {
        val root = floatingRootView ?: return
        val llBubble = root.findViewById<LinearLayout>(R.id.ll_minimized_bubble)
        val btnCollapse = root.findViewById<Button>(R.id.btn_hud_collapse)
        val btnPlayPause = root.findViewById<Button>(R.id.btn_hud_play_pause)
        val btnStop = root.findViewById<Button>(R.id.btn_hud_stop)
        val btnSettings = root.findViewById<Button>(R.id.btn_hud_settings)
        val btnScreenOff = root.findViewById<Button>(R.id.btn_hud_screen_off)
        val btnConsole = root.findViewById<Button>(R.id.btn_hud_console)
        val btnExit = root.findViewById<Button>(R.id.btn_hud_exit)
        val tvStatus = root.findViewById<TextView>(R.id.tv_hud_status_badge)

        updateExpandedState(isExpanded)

        llBubble?.setOnClickListener { updateExpandedState(true) }
        btnCollapse?.setOnClickListener { updateExpandedState(false) }

        btnPlayPause?.setOnClickListener {
            try {
                if (automationCore == null) initCore()
                val core = automationCore
                if (core == null) {
                    Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!core.isRunning) {
                    core.startLoop(accounts = 1)
                    btnPlayPause.text = "⏸"
                    tvStatus?.text = "FARM"
                    Toast.makeText(this, "🚀 Ari AI Loot Farm Started!", Toast.LENGTH_SHORT).show()
                } else {
                    core.stopLoop()
                    btnPlayPause.text = "▶"
                    tvStatus?.text = "PAUSED"
                    Toast.makeText(this, "⏸ Ari AI Paused", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FloatingHUD", "Play/Pause error: ${e.message}")
            }
        }

        btnStop?.setOnClickListener {
            stopMacroExecution()
            Toast.makeText(this, "⏹ Farm Loops Reset & Stopped", Toast.LENGTH_SHORT).show()
        }

        btnSettings?.setOnClickListener {
            try {
                val core = automationCore
                val batt = core?.watchdog?.batteryPercentage ?: 100
                val temp = core?.watchdog?.currentTemperatureCelsius ?: 30.0f
                val raids = core?.totalRaidsCompleted ?: 0
                Toast.makeText(this, "⚙ [MACRORIFY SETTINGS] ⚔️ Raids: $raids | 🔋 $batt% | 🌡️ ${temp}°C | 0-Training Active", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Settings Active", Toast.LENGTH_SHORT).show()
            }
        }

        btnScreenOff?.setOnClickListener {
            toggleBlackScreenSaver()
        }

        btnConsole?.setOnClickListener {
            val core = automationCore
            val lastRaid = core?.totalRaidsCompleted ?: 0
            val running = core?.isRunning ?: false
            Toast.makeText(this, "▶_ [CONSOLE] State: ${if (running) "FARMING" else "IDLE"} | Total Raids: $lastRaid | Vision: OCR & Cloud Active", Toast.LENGTH_SHORT).show()
        }

        btnExit?.setOnClickListener {
            stopSelf()
        }

        setupTouchAndDragGesture()
    }

    private fun toggleBlackScreenSaver() {
        if (!isBlackScreenActive) {
            try {
                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val blackParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                )
                blackParams.screenBrightness = 0.01f

                val blackView = FrameLayout(this).apply {
                    setBackgroundColor(Color.BLACK)
                    val tv = TextView(this@FloatingHUDService).apply {
                        text = "🔋 Black Screen Battery Saver Active\n(Double-tap to dismiss)"
                        setTextColor(Color.DKGRAY)
                        textSize = 12f
                        gravity = Gravity.CENTER
                    }
                    addView(tv, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
                    setOnClickListener {
                        toggleBlackScreenSaver()
                    }
                }

                windowManager?.addView(blackView, blackParams)
                blackScreenView = blackView
                isBlackScreenActive = true
                Toast.makeText(this, "🌙 Black Screen Saver Active (Tap to wake)", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("FloatingHUD", "Error enabling black screen: ${e.message}")
            }
        } else {
            try {
                if (blackScreenView != null) {
                    windowManager?.removeView(blackScreenView)
                    blackScreenView = null
                }
                isBlackScreenActive = false
                Toast.makeText(this, "☀️ Screen Woken", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("FloatingHUD", "Error removing black screen: ${e.message}")
            }
        }
    }

    private fun updateExpandedState(expanded: Boolean) {
        isExpanded = expanded
        try {
            prefs.edit().putBoolean("is_expanded", isExpanded).apply()
            val llBubble = floatingRootView?.findViewById<LinearLayout>(R.id.ll_minimized_bubble)
            val llToolbar = floatingRootView?.findViewById<LinearLayout>(R.id.ll_expanded_toolbar)

            if (expanded) {
                llBubble?.visibility = View.GONE
                llToolbar?.visibility = View.VISIBLE
            } else {
                llToolbar?.visibility = View.GONE
                llBubble?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.w("FloatingHUD", "Error updating expanded state: ${e.message}")
        }
    }

    private fun setupTouchAndDragGesture() {
        floatingRootView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                val params = windowParams ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        if (abs(dx) > 12 || abs(dy) > 12) {
                            isDragging = true
                            params.x = (initialX + dx).toInt().coerceIn(0, screenWidth - 80)
                            params.y = (initialY + dy).toInt().coerceIn(0, screenHeight - 80)
                            try {
                                if (isViewAttached) windowManager?.updateViewLayout(floatingRootView, params)
                            } catch (e: Exception) {}
                            return true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isDragging) {
                            snapToClosestEdge(params.x)
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun snapToClosestEdge(currentX: Int) {
        val params = windowParams ?: return
        val targetX = if (currentX + 150 < screenWidth / 2) 20 else (screenWidth - (floatingRootView?.width ?: 200) - 20)

        val animator = ValueAnimator.ofInt(params.x, targetX)
        animator.duration = 250L
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            params.x = animation.animatedValue as Int
            try {
                if (isViewAttached) windowManager?.updateViewLayout(floatingRootView, params)
            } catch (e: Exception) {}
        }
        animator.start()

        try {
            prefs.edit().putInt("pos_x", targetX).putInt("pos_y", params.y).apply()
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            automationCore?.stopLoop()
            if (isBlackScreenActive && blackScreenView != null) {
                windowManager?.removeView(blackScreenView)
                blackScreenView = null
            }
            if (isViewAttached && floatingRootView != null) {
                windowManager?.removeView(floatingRootView)
                isViewAttached = false
            }
        } catch (e: Exception) {
            Log.w("FloatingHUD", "Error on destroy: ${e.message}")
        }
    }
}
