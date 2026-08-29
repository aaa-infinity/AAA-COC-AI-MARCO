package com.cocai.autoclicker.engine

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
