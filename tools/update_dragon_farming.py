import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

# 1. Update CocFarmingEngine.kt with dedicated Dragon Army Farming
coc_engine = """package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

enum class CocStrategy {
    ZAP_DRAGON_FARMING,      // Premier Home Village Dragon & Zap Attack
    ELECTRO_DRAGON_SPAM,     // Chain Lightning E-Drag core wipeout
    DRAGON_RIDER_SMASH,      // High TH Dragon + Dragon Rider air assault
    SNEAKY_GOBLIN_ORE_FARM   // Quick 1-Star & Ores
}

class CocFarmingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    val modernFeatures = ModernCocFeatures(accessibilityService)

    var isRunning: Boolean = false
        private set

    var currentStrategy: CocStrategy = CocStrategy.ZAP_DRAGON_FARMING
    var goldCollected: Long = 0
    var elixirCollected: Long = 0
    var darkElixirCollected: Long = 0
    var raidsCompleted: Int = 0

    fun startEngine(strategy: CocStrategy = CocStrategy.ZAP_DRAGON_FARMING) {
        currentStrategy = strategy
        isRunning = true
        Log.i("CocEngine", "Starting Home Village Dragon Farming Engine with: " + strategy.name)
        scheduleNextStep(800L) {
            runHomeVillageLoop()
        }
    }

    fun stopEngine() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        Log.i("CocEngine", "Dragon Farming Engine stopped.")
    }

    private fun scheduleNextStep(delayMs: Long, action: () -> Unit) {
        if (!isRunning) return
        val randomizedDelay = delayMs + Random.nextLong(100L, 350L)
        handler.postDelayed({
            if (isRunning) action()
        }, randomizedDelay)
    }

    /**
     * Complete Home Village Loop:
     * 1. Collect Mines, Pumps, Drills, Treasury, Ores
     * 2. 0-Cost Quick Train Dragon Army
     * 3. Multiplayer Matchmaking
     * 4. Execute Dragon Battle Deployment
     * 5. Return Home & Loop
     */
    private fun runHomeVillageLoop() {
        Log.i("CocEngine", "=== [HOME VILLAGE] Collecting Resources & Daily Ores ===")

        collectHomeVillageResources {
            trainDragonArmy {
                when (currentStrategy) {
                    CocStrategy.ZAP_DRAGON_FARMING -> executeZapDragonAttack()
                    CocStrategy.ELECTRO_DRAGON_SPAM -> executeElectroDragonAttack()
                    CocStrategy.DRAGON_RIDER_SMASH -> executeDragonRiderAttack()
                    CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> executeSneakyGoblinAttack()
                }
            }
        }
    }

    private fun collectHomeVillageResources(onComplete: () -> Unit) {
        val tapPoints = listOf(
            Pair(750f, 450f),   // Gold Mine
            Pair(950f, 520f),   // Elixir Collector
            Pair(1150f, 480f),  // Dark Elixir Drill
            Pair(850f, 650f),   // Gem Mine
            Pair(1600f, 900f)   // Treasury & Star Bonus Ores
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

    private fun trainDragonArmy(onComplete: () -> Unit) {
        Log.i("CocEngine", "Queuing 0-Cost Dragon Army Preset...")
        // Tap Train Icon (bottom-left)
        accessibilityService.performTap(90f, 830f) {
            scheduleNextStep(1000L) {
                // Tap Quick Train Tab
                accessibilityService.performTap(1350f, 150f) {
                    scheduleNextStep(700L) {
                        // Tap Train Slot #1 (Dragon Army Preset)
                        accessibilityService.performTap(1580f, 380f) {
                            scheduleNextStep(700L) {
                                // Close Window
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
     * Dedicated Home Village Zap Dragon Attack:
     * 1. Zap Lightning Spells on top Air Defenses
     * 2. Funnel King & Queen on outer corners
     * 3. Spread Dragons in a clean line
     * 4. Drop Balloons & Grand Warden behind Dragons
     * 5. Activate Grand Warden & Hero Equipment
     * 6. Collect 100% Home Village loot
     */
    private fun executeZapDragonAttack() {
        Log.i("CocEngine", "Starting Home Village Zap Dragon Raid...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                // Step 1: Zap Air Defenses (Slot 5: Lightning Spells)
                Log.i("CocEngine", "Step 1: Destroying Air Defenses with Lightning Spells...")
                accessibilityService.performTap(620f, 980f) // Slot 5: Lightning Spell
                val ad1 = Pair(750f, 480f)
                val ad2 = Pair(1170f, 480f)
                
                accessibilityService.performTap(ad1.first, ad1.second)
                scheduleNextStep(200L) { accessibilityService.performTap(ad1.first, ad1.second) }
                scheduleNextStep(400L) { accessibilityService.performTap(ad1.first, ad1.second) }
                scheduleNextStep(600L) { accessibilityService.performTap(ad2.first, ad2.second) }
                scheduleNextStep(800L) { accessibilityService.performTap(ad2.first, ad2.second) }
                scheduleNextStep(1000L) { accessibilityService.performTap(ad2.first, ad2.second) }

                scheduleNextStep(1500L) {
                    // Step 2: Funnel Corner Heroes (King & Queen on opposite corners)
                    Log.i("CocEngine", "Step 2: Creating corner funnels with Heroes...")
                    accessibilityService.performTap(300f, 980f) // King
                    accessibilityService.performTap(450f, 850f)
                    accessibilityService.performTap(400f, 980f) // Queen
                    accessibilityService.performTap(1450f, 850f)

                    scheduleNextStep(1000L) {
                        // Step 3: Line Deployment of Dragons (Slot 1)
                        Log.i("CocEngine", "Step 3: Spreading Dragons in wide line...")
                        val dragonLine = listOf(
                            Pair(600f, 820f), Pair(750f, 830f), Pair(900f, 840f),
                            Pair(1050f, 840f), Pair(1200f, 830f), Pair(1350f, 820f)
                        )
                        accessibilityService.performTap(200f, 980f) // Slot 1: Dragons
                        accessibilityService.performMultiTouchTaps(dragonLine)

                        scheduleNextStep(1200L) {
                            // Step 4: Drop Balloons (Slot 2) & Grand Warden behind dragons
                            Log.i("CocEngine", "Step 4: Deploying Balloons & Grand Warden...")
                            accessibilityService.performTap(290f, 980f) // Slot 2: Balloons
                            accessibilityService.performMultiTouchTaps(dragonLine)

                            accessibilityService.performTap(500f, 980f) // Grand Warden
                            accessibilityService.performTap(960f, 850f)

                            // Step 5: Rage Spell into base core (Slot 6)
                            scheduleNextStep(8000L) {
                                Log.i("CocEngine", "Step 5: Casting Rage Spell in core...")
                                accessibilityService.performTap(720f, 980f)
                                accessibilityService.performTap(960f, 540f)

                                // Step 6: Trigger Hero Equipment & Warden Ability
                                scheduleNextStep(8000L) {
                                    Log.i("CocEngine", "Step 6: Triggering Hero Equipment abilities...")
                                    modernFeatures.triggerHeroEquipmentCombos()

                                    // Wait for dragon destruction & exit
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
    }

    private fun executeElectroDragonAttack() {
        Log.i("CocEngine", "Starting Electro Dragon Attack...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                val eDragLine = listOf(Pair(650f, 820f), Pair(850f, 840f), Pair(1070f, 840f), Pair(1270f, 820f))
                accessibilityService.performTap(200f, 980f) // E-Drags
                accessibilityService.performMultiTouchTaps(eDragLine)
                scheduleNextStep(1500L) {
                    deployHeroes(Pair(960f, 850f))
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

    private fun executeDragonRiderAttack() {
        Log.i("CocEngine", "Starting Dragon Rider Attack...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                val line = listOf(Pair(700f, 800f), Pair(900f, 820f), Pair(1100f, 820f), Pair(1300f, 800f))
                accessibilityService.performTap(200f, 980f) // Dragons
                accessibilityService.performMultiTouchTaps(line)
                accessibilityService.performTap(290f, 980f) // Dragon Riders
                accessibilityService.performMultiTouchTaps(line)
                scheduleNextStep(1500L) {
                    deployHeroes(Pair(960f, 850f))
                    scheduleNextStep(14000L) {
                        modernFeatures.triggerHeroEquipmentCombos()
                        scheduleNextStep(38000L) {
                            surrenderAndReturnHome()
                        }
                    }
                }
            }
        }
    }

    private fun executeSneakyGoblinAttack() {
        startMultiplayerMatchmaking {
            scheduleNextStep(4000L) {
                val perimeter = listOf(
                    Pair(400f, 300f), Pair(600f, 200f), Pair(960f, 150f),
                    Pair(1300f, 200f), Pair(1500f, 300f), Pair(1600f, 600f)
                )
                accessibilityService.performTap(200f, 980f)
                accessibilityService.performMultiTouchTaps(perimeter)
                scheduleNextStep(12000L) {
                    surrenderAndReturnHome()
                }
            }
        }
    }

    private fun startMultiplayerMatchmaking(onMatchLoaded: () -> Unit) {
        accessibilityService.performTap(120f, 950f) {
            scheduleNextStep(1400L) {
                accessibilityService.performTap(1450f, 650f) {
                    onMatchLoaded()
                }
            }
        }
    }

    private fun deployHeroes(dropCoord: Pair<Float, Float>) {
        for (slotX in listOf(300f, 400f, 500f, 600f)) {
            accessibilityService.performTap(slotX, 980f)
            accessibilityService.performTap(dropCoord.first, dropCoord.second)
        }
    }

    private fun surrenderAndReturnHome() {
        Log.i("CocEngine", "Harvest complete. Returning to Home Village...")
        accessibilityService.performTap(120f, 880f) {
            scheduleNextStep(800L) {
                accessibilityService.performTap(1100f, 680f) {
                    scheduleNextStep(2200L) {
                        accessibilityService.performTap(960f, 920f) {
                            raidsCompleted++
                            scheduleNextStep(4000L) {
                                runHomeVillageLoop()
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

# 2. Update floating_hud.xml for Dragon Farming
hud_xml = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#F20A0F1D"
    android:padding="10dp"
    android:elevation="12dp">

    <!-- Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="🐉 AAA COC AI - DRAGON FARM"
            android:textColor="#F59E0B"
            android:textSize="11sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tv_hud_status"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="[IDLE]"
            android:textColor="#10B981"
            android:textSize="10sp"
            android:textStyle="bold" />
    </LinearLayout>

    <TextView
        android:id="@+id/tv_ore_stats"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="3dp"
        android:text="🏰 Home Village | Zap Dragons Ready"
        android:textColor="#94A3B8"
        android:textSize="9sp" />

    <!-- Button Row -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="6dp">

        <Button
            android:id="@+id/btn_strategy"
            android:layout_width="wrap_content"
            android:layout_height="32dp"
            android:backgroundTint="#DC2626"
            android:text="ZAP DRAGON FARMING"
            android:textColor="#FFFFFF"
            android:textSize="9sp"
            android:paddingHorizontal="6dp" />

        <Button
            android:id="@+id/btn_toggle_play"
            android:layout_width="wrap_content"
            android:layout_height="32dp"
            android:layout_marginStart="6dp"
            android:backgroundTint="#10B981"
            android:text="▶ START"
            android:textColor="#FFFFFF"
            android:textSize="9sp"
            android:textStyle="bold"
            android:paddingHorizontal="8dp" />
    </LinearLayout>

</LinearLayout>
"""

with open('android_app/app/src/main/res/layout/floating_hud.xml', 'w') as f:
    f.write(hud_xml)

# 3. Update FloatingOverlayService.kt to handle dragon strategies
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

        var selectedStrategy = CocStrategy.ZAP_DRAGON_FARMING

        btnStrategy?.setOnClickListener {
            selectedStrategy = when (selectedStrategy) {
                CocStrategy.ZAP_DRAGON_FARMING -> CocStrategy.ELECTRO_DRAGON_SPAM
                CocStrategy.ELECTRO_DRAGON_SPAM -> CocStrategy.DRAGON_RIDER_SMASH
                CocStrategy.DRAGON_RIDER_SMASH -> CocStrategy.SNEAKY_GOBLIN_ORE_FARM
                CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> CocStrategy.ZAP_DRAGON_FARMING
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
                    tvStatus?.text = "[FARMING]"
                }
            } else {
                tvStatus?.text = "[ENABLE ACCESSIBILITY]"
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

print("Dragon farming engine and floating HUD configured successfully.")
