import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

# 1. Update CocFarmingEngine.kt
coc_engine = """package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

enum class CocStrategy {
    OVERGROWTH_ROOT_RIDER,   // TH16/TH17 Meta: Druid + Root Rider + Overgrowth Spell
    SNEAKY_GOBLIN_ORE_FARM,  // Fast Collector + Town Hall 1-Star for Ores
    TH17_HERO_SMASH,         // 5-Hero Hall Smash with Minion Prince & Equipment
    BUILDER_BASE_FAST_FARM,  // Builder Base 2.0 Infinite Loot
    BARCH_PERIMETER_FARM     // Classic / Low TH Farming
}

class CocFarmingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    val modernFeatures = ModernCocFeatures(accessibilityService)

    var isRunning: Boolean = false
        private set

    var currentStrategy: CocStrategy = CocStrategy.OVERGROWTH_ROOT_RIDER
    var goldCollected: Long = 0
    var elixirCollected: Long = 0
    var darkElixirCollected: Long = 0
    var shinyOresCollected: Long = 0
    var glowyOresCollected: Long = 0
    var starryOresCollected: Long = 0
    var raidsCompleted: Int = 0

    fun startEngine(strategy: CocStrategy) {
        currentStrategy = strategy
        isRunning = true
        Log.i("CocEngine", "Starting Modern CoC Engine with: " + strategy.name)
        scheduleNextStep(800L) {
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
        val randomizedDelay = delayMs + Random.nextLong(100L, 350L)
        handler.postDelayed({
            if (isRunning) action()
        }, randomizedDelay)
    }

    /**
     * Modern Village Routine:
     * 1. Collect Standard Mines + Drills + Treasury + Daily Star Bonus (Ores)
     * 2. Auto-craft Capital Gold at Forge if storages full
     * 3. 0-Cost Quick Train Army
     * 4. Dispatch to Selected Strategy Attack
     */
    private fun runVillageLoop() {
        Log.i("CocEngine", "=== [VILLAGE] Collecting Resources & Ores ===")

        collectVillageResourcesAndOres {
            trainPresetArmy {
                when (currentStrategy) {
                    CocStrategy.BUILDER_BASE_FAST_FARM -> executeBuilderBaseFastFarm()
                    CocStrategy.OVERGROWTH_ROOT_RIDER -> executeOvergrowthRootRiderRaid()
                    CocStrategy.TH17_HERO_SMASH -> executeTH17HeroSmashRaid()
                    CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> executeSneakyGoblinOreRaid()
                    CocStrategy.BARCH_PERIMETER_FARM -> executeBarchPerimeterRaid()
                }
            }
        }
    }

    private fun collectVillageResourcesAndOres(onComplete: () -> Unit) {
        val tapPoints = listOf(
            Pair(750f, 450f),   // Gold Mine
            Pair(950f, 520f),   // Elixir Collector
            Pair(1150f, 480f),  // Dark Elixir Drill
            Pair(850f, 650f),   // Gem Mine
            Pair(1600f, 900f),  // Treasury / Daily Star Bonus Ores
            Pair(1450f, 450f)   // Loot Cart
        )

        var idx = 0
        fun tapNext() {
            if (idx < tapPoints.size && isRunning) {
                val pt = tapPoints[idx++]
                accessibilityService.performTap(pt.first, pt.second) {
                    scheduleNextStep(350L) { tapNext() }
                }
            } else {
                onComplete()
            }
        }
        tapNext()
    }

    private fun trainPresetArmy(onComplete: () -> Unit) {
        // Tap Train Icon (bottom-left)
        accessibilityService.performTap(90f, 830f) {
            scheduleNextStep(1000L) {
                // Tap Quick Train Tab
                accessibilityService.performTap(1350f, 150f) {
                    scheduleNextStep(700L) {
                        // Tap Train Slot #1 (0-Cost Instant Queue)
                        accessibilityService.performTap(1580f, 380f) {
                            scheduleNextStep(700L) {
                                // Tap Close Window Button
                                accessibilityService.performTap(1820f, 85f) {
                                    scheduleNextStep(900L, onComplete)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Modern TH16/TH17 Strategy: Druids + Root Riders + Overgrowth Spell + Equipment
     */
    private fun executeOvergrowthRootRiderRaid() {
        Log.i("CocEngine", "Starting Overgrowth Root Rider Attack...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                // 1. Cast Overgrowth Spell on high-threat flank defense (Monolith / Inferno Artillery)
                modernFeatures.deployOvergrowthSpell(Pair(1200f, 400f), slotIndex = 7)
                scheduleNextStep(1000L) {
                    // 2. Deploy Root Riders (Slot 1) in line
                    val entryLine = listOf(Pair(600f, 750f), Pair(700f, 780f), Pair(800f, 800f), Pair(900f, 820f))
                    accessibilityService.performTap(200f, 980f) // Slot 1: Root Riders
                    accessibilityService.performMultiTouchTaps(entryLine)

                    scheduleNextStep(1200L) {
                        // 3. Deploy Druids (Slot 2) behind Root Riders
                        accessibilityService.performTap(300f, 980f) // Slot 2: Druids
                        accessibilityService.performMultiTouchTaps(entryLine)

                        scheduleNextStep(1500L) {
                            // 4. Deploy Heroes & Hero Hall 5th Hero Minion Prince
                            deployAllHeroes(entryLine[1])

                            scheduleNextStep(14000L) {
                                // 5. Trigger Hero Equipment Combos (Giant Gauntlet, Magic Mirror, Fireball)
                                modernFeatures.triggerHeroEquipmentCombos()

                                scheduleNextStep(38000L) {
                                    surrenderAndReturnHome()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * TH17 Hero Hall Smash with Minion Prince flying hero + Flame Flinger
     */
    private fun executeTH17HeroSmashRaid() {
        Log.i("CocEngine", "Starting TH17 Hero Hall Smash...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                val dropCoord = Pair(800f, 800f)
                // Deploy Siege Machine (Slot 6)
                accessibilityService.performTap(700f, 980f)
                accessibilityService.performTap(dropCoord.first, dropCoord.second)

                scheduleNextStep(1500L) {
                    // Deploy all 5 heroes
                    deployAllHeroes(dropCoord)
                    scheduleNextStep(15000L) {
                        modernFeatures.triggerHeroEquipmentCombos()
                        scheduleNextStep(40000L) {
                            surrenderAndReturnHome()
                        }
                    }
                }
            }
        }
    }

    /**
     * Sneaky Goblin Star Bonus & Ore Rush
     */
    private fun executeSneakyGoblinOreRaid() {
        Log.i("CocEngine", "Starting Sneaky Goblin Ore Farming...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4000L) {
                val perimeter = listOf(
                    Pair(400f, 300f), Pair(600f, 200f), Pair(960f, 150f),
                    Pair(1300f, 200f), Pair(1500f, 300f), Pair(1600f, 600f),
                    Pair(1400f, 800f), Pair(960f, 900f), Pair(500f, 800f)
                )

                // Select Sneaky Goblins (Slot 1)
                accessibilityService.performTap(200f, 980f)
                accessibilityService.performMultiTouchTaps(perimeter)

                scheduleNextStep(3000L) {
                    // Core Jump / Invisibility to secure Town Hall 1-Star for Ore Star Bonus
                    accessibilityService.performTap(620f, 980f)
                    accessibilityService.performTap(960f, 540f)

                    scheduleNextStep(12000L) {
                        modernFeatures.triggerHeroEquipmentCombos()
                        scheduleNextStep(20000L) {
                            surrenderAndReturnHome()
                        }
                    }
                }
            }
        }
    }

    /**
     * Builder Base 2.0 Ultra-Fast Farming (Infinite Builder Gold/Elixir loop)
     */
    private fun executeBuilderBaseFastFarm() {
        Log.i("CocEngine", "Starting Builder Base 2.0 Fast Farming Loop...")
        accessibilityService.performTap(120f, 950f) {
            scheduleNextStep(1500L) {
                accessibilityService.performTap(1400f, 600f) { // Find Builder Match
                    scheduleNextStep(4000L) {
                        val spots = listOf(Pair(500f, 400f), Pair(960f, 250f), Pair(1400f, 400f), Pair(960f, 800f))
                        accessibilityService.performTap(200f, 980f)
                        accessibilityService.performMultiTouchTaps(spots)
                        accessibilityService.performTap(300f, 980f)
                        accessibilityService.performMultiTouchTaps(spots)

                        scheduleNextStep(2000L) {
                            surrenderAndReturnHome()
                        }
                    }
                }
            }
        }
    }

    private fun executeBarchPerimeterRaid() {
        startMultiplayerMatchmaking {
            scheduleNextStep(4000L) {
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
        }
    }

    private fun startMultiplayerMatchmaking(onMatchLoaded: () -> Unit) {
        // Tap Attack Button (bottom-left)
        accessibilityService.performTap(120f, 950f) {
            scheduleNextStep(1400L) {
                // Tap Find Match (Multiplayer)
                accessibilityService.performTap(1450f, 650f) {
                    onMatchLoaded()
                }
            }
        }
    }

    private fun deployAllHeroes(dropCoord: Pair<Float, Float>) {
        val heroSlots = listOf(300f, 400f, 500f, 600f, 700f)
        for (slotX in heroSlots) {
            accessibilityService.performTap(slotX, 980f)
            accessibilityService.performTap(dropCoord.first, dropCoord.second)
        }
    }

    private fun surrenderAndReturnHome() {
        Log.i("CocEngine", "Ending battle to return home...")
        accessibilityService.performTap(120f, 880f) { // End Battle
            scheduleNextStep(800L) {
                accessibilityService.performTap(1100f, 680f) { // OK
                    scheduleNextStep(2200L) {
                        accessibilityService.performTap(960f, 920f) { // Return Home
                            raidsCompleted++
                            scheduleNextStep(4000L) {
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

with open(f'{pkg_dir}/engine/CocFarmingEngine.kt', 'w') as f:
    f.write(coc_engine)

# 2. Update floating_hud.xml with modern controls
floating_hud_xml = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#EE0F172A"
    android:padding="10dp"
    android:elevation="8dp">

    <!-- Header / Drag handle -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="⚔ CoC Modern AI"
            android:textColor="#F59E0B"
            android:textSize="12sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tv_hud_status"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="[IDLE]"
            android:textColor="#10B981"
            android:textSize="11sp"
            android:textStyle="bold" />
    </LinearLayout>

    <!-- Stats Row (Ores + Loot) -->
    <TextView
        android:id="@+id/tv_ore_stats"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:text="💎 Ores: Star Bonus Ready | 0-Cost Army"
        android:textColor="#94A3B8"
        android:textSize="10sp" />

    <!-- Button Row -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="8dp">

        <Button
            android:id="@+id/btn_strategy"
            android:layout_width="wrap_content"
            android:layout_height="34dp"
            android:backgroundTint="#6366F1"
            android:text="OVERGROWTH ROOT RIDER"
            android:textColor="#FFFFFF"
            android:textSize="10sp"
            android:paddingHorizontal="8dp" />

        <Button
            android:id="@+id/btn_toggle_play"
            android:layout_width="wrap_content"
            android:layout_height="34dp"
            android:layout_marginStart="6dp"
            android:backgroundTint="#10B981"
            android:text="▶ START"
            android:textColor="#FFFFFF"
            android:textSize="10sp"
            android:textStyle="bold"
            android:paddingHorizontal="10dp" />
    </LinearLayout>

</LinearLayout>
"""

with open('android_app/app/src/main/res/layout/floating_hud.xml', 'w') as f:
    f.write(floating_hud_xml)

# 3. Update FloatingOverlayService.kt to handle strategy cycle and stats
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
            x = 80
            y = 180
        }

        windowManager.addView(floatingView, params)

        val btnToggle = floatingView?.findViewById<Button>(R.id.btn_toggle_play)
        val btnStrategy = floatingView?.findViewById<Button>(R.id.btn_strategy)
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)

        var selectedStrategy = CocStrategy.OVERGROWTH_ROOT_RIDER

        btnStrategy?.setOnClickListener {
            selectedStrategy = when (selectedStrategy) {
                CocStrategy.OVERGROWTH_ROOT_RIDER -> CocStrategy.SNEAKY_GOBLIN_ORE_FARM
                CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> CocStrategy.TH17_HERO_SMASH
                CocStrategy.TH17_HERO_SMASH -> CocStrategy.BUILDER_BASE_FAST_FARM
                CocStrategy.BUILDER_BASE_FAST_FARM -> CocStrategy.BARCH_PERIMETER_FARM
                CocStrategy.BARCH_PERIMETER_FARM -> CocStrategy.OVERGROWTH_ROOT_RIDER
            }
            btnStrategy.text = selectedStrategy.name.replace("_", " ")
        }

        btnToggle?.setOnClickListener {
            val engine = farmingEngine
            if (engine != null) {
                if (engine.isRunning) {
                    engine.stopEngine()
                    btnToggle.text = "▶ START"
                    tvStatus?.text = "[IDLE]"
                } else {
                    engine.startEngine(selectedStrategy)
                    btnToggle.text = "⏸ PAUSE"
                    tvStatus?.text = "[RUNNING]"
                }
            } else {
                tvStatus?.text = "[NO ACCESSIBILITY]"
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

with open(f'{pkg_dir}/service/FloatingOverlayService.kt', 'w') as f:
    f.write(floating_service)

print("Updated CocFarmingEngine, FloatingOverlayService, and floating_hud.xml")
