package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

enum class CocStrategy {
    ZAP_DRAGON_FARMING,      // Premier Home Village Dragon & Zap Attack with 4-Finger Multi-Touch
    ELECTRO_DRAGON_SPAM,     // Chain Lightning E-Drag core wipeout
    DRAGON_RIDER_SMASH,      // High TH Dragon + Dragon Rider air assault
    SNEAKY_GOBLIN_ORE_FARM   // Quick 1-Star & Ores with Multi-Touch Perimeter Drop
}

class CocFarmingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    val modernFeatures = ModernCocFeatures(accessibilityService)
    val multiTouch = MultiTouchDeployer(accessibilityService)

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
        Log.i("CocEngine", "Starting Home Village Autonomous Farming Engine with Multi-Touch: " + strategy.name)
        scheduleNextStep(800L) {
            runHomeVillageLoop()
        }
    }

    fun stopEngine() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        Log.i("CocEngine", "Autonomous Farming Engine stopped.")
    }

    private fun scheduleNextStep(delayMs: Long, action: () -> Unit) {
        if (!isRunning) return
        val randomizedDelay = delayMs + Random.nextLong(100L, 350L)
        handler.postDelayed({
            if (isRunning) action()
        }, randomizedDelay)
    }

    /**
     * Complete Home Village Autonomous Loop:
     * 1. Collect Mines, Pumps, Drills, Treasury, Ores
     * 2. 0-Cost Quick Train Dragon Army
     * 3. Multiplayer Matchmaking
     * 4. Multi-Touch Simultaneous Deployment (4-Finger Wave + 2-Finger Funnel)
     * 5. Return Home & Self-Independent Loop
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
            PointF(750f, 450f),   // Gold Mine
            PointF(950f, 520f),   // Elixir Collector
            PointF(1150f, 480f),  // Dark Elixir Drill
            PointF(850f, 650f),   // Gem Mine
            PointF(1600f, 900f)   // Treasury & Star Bonus Ores
        )

        var idx = 0
        fun tapNext() {
            if (idx < tapPoints.size && isRunning) {
                val pt = tapPoints[idx++]
                accessibilityService.performTap(pt.x, pt.y) {
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
     * Dedicated Home Village Zap Dragon Attack with Multi-Touch:
     * 1. Zap Lightning Spells on top Air Defenses
     * 2. 2-Finger Simultaneous Corner Funnel (King Left, Queen Right)
     * 3. 4-Finger Simultaneous Dragon Wave Line
     * 4. Multi-Touch Balloon & Grand Warden drop
     * 5. Activate Grand Warden & Hero Equipment
     * 6. Collect 100% Home Village loot
     */
    private fun executeZapDragonAttack() {
        Log.i("CocEngine", "Starting Home Village Multi-Touch Zap Dragon Raid...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                // Step 1: Zap Air Defenses (Slot 5: Lightning Spells)
                Log.i("CocEngine", "Step 1: Destroying Air Defenses with Lightning Spells...")
                accessibilityService.performTap(620f, 980f) // Slot 5: Lightning Spell
                val ad1 = PointF(750f, 480f)
                val ad2 = PointF(1170f, 480f)
                
                accessibilityService.performTap(ad1.x, ad1.y)
                scheduleNextStep(200L) { accessibilityService.performTap(ad1.x, ad1.y) }
                scheduleNextStep(400L) { accessibilityService.performTap(ad1.x, ad1.y) }
                scheduleNextStep(600L) { accessibilityService.performTap(ad2.x, ad2.y) }
                scheduleNextStep(800L) { accessibilityService.performTap(ad2.x, ad2.y) }
                scheduleNextStep(1000L) { accessibilityService.performTap(ad2.x, ad2.y) }

                scheduleNextStep(1500L) {
                    // Step 2: 2-Finger Simultaneous Corner Funnel
                    Log.i("CocEngine", "Step 2: 2-Finger Simultaneous Hero Funnel...")
                    accessibilityService.performTap(300f, 980f) // Select King
                    multiTouch.deployTwoFingerFunnel(
                        leftCorner = PointF(450f, 850f),
                        rightCorner = PointF(1450f, 850f),
                        taps = 2
                    ) {
                        scheduleNextStep(1000L) {
                            // Step 3: 4-Finger Simultaneous Dragon Wave (Slot 1)
                            Log.i("CocEngine", "Step 3: 4-Finger Simultaneous Dragon Wave...")
                            accessibilityService.performTap(200f, 980f) // Slot 1: Dragons
                            multiTouch.deployFourFingerWave(
                                startCorner = PointF(550f, 830f),
                                endCorner = PointF(1380f, 830f),
                                waves = 3
                            ) {
                                scheduleNextStep(800L) {
                                    // Step 4: Multi-Touch Balloons (Slot 2) & Grand Warden
                                    Log.i("CocEngine", "Step 4: Deploying Balloons & Grand Warden...")
                                    accessibilityService.performTap(290f, 980f) // Slot 2: Balloons
                                    multiTouch.deployFourFingerWave(
                                        startCorner = PointF(650f, 840f),
                                        endCorner = PointF(1280f, 840f),
                                        waves = 2
                                    ) {
                                        accessibilityService.performTap(500f, 980f) // Grand Warden
                                        accessibilityService.performTap(960f, 850f)

                                        // Step 5: Rage Spell into base core (Slot 6)
                                        scheduleNextStep(7000L) {
                                            Log.i("CocEngine", "Step 5: Casting Rage Spell in core...")
                                            accessibilityService.performTap(720f, 980f)
                                            accessibilityService.performTap(960f, 540f)

                                            // Step 6: Trigger Hero Equipment & Warden Ability
                                            scheduleNextStep(7000L) {
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
            }
        }
    }

    private fun executeElectroDragonAttack() {
        Log.i("CocEngine", "Starting Electro Dragon Attack with Multi-Touch...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                accessibilityService.performTap(200f, 980f) // E-Drags
                multiTouch.deployFourFingerWave(
                    startCorner = PointF(600f, 830f),
                    endCorner = PointF(1350f, 830f),
                    waves = 2
                ) {
                    scheduleNextStep(1200L) {
                        deployHeroes(PointF(960f, 850f))
                        scheduleNextStep(14000L) {
                            modernFeatures.triggerHeroEquipmentCombos()
                            scheduleNextStep(40000L) {
                                surrenderAndReturnHome()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun executeDragonRiderAttack() {
        Log.i("CocEngine", "Starting Dragon Rider Attack with Multi-Touch...")
        startMultiplayerMatchmaking {
            scheduleNextStep(4500L) {
                accessibilityService.performTap(200f, 980f) // Dragons
                multiTouch.deployFourFingerWave(PointF(650f, 830f), PointF(1300f, 830f), 2) {
                    accessibilityService.performTap(290f, 980f) // Dragon Riders
                    multiTouch.deployFourFingerWave(PointF(700f, 840f), PointF(1250f, 840f), 2) {
                        scheduleNextStep(1200L) {
                            deployHeroes(PointF(960f, 850f))
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
        }
    }

    private fun executeSneakyGoblinAttack() {
        startMultiplayerMatchmaking {
            scheduleNextStep(4000L) {
                val perimeter = listOf(
                    PointF(400f, 300f), PointF(600f, 200f), PointF(960f, 150f),
                    PointF(1300f, 200f), PointF(1500f, 300f), PointF(1600f, 600f)
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

    private fun deployHeroes(dropCoord: PointF) {
        for (slotX in listOf(300f, 400f, 500f, 600f)) {
            accessibilityService.performTap(slotX, 980f)
            accessibilityService.performTap(dropCoord.x, dropCoord.y)
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
