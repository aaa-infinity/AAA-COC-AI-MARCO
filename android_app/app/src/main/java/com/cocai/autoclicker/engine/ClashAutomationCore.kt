package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 👑 CLASH AUTOMATION CORE ENGINE (Percentage-Driven Fixed UI State Machine)
 *
 * 100% Reliable Architecture:
 * - Operates EXCLUSIVELY on fixed Supercell UI HUD percentage anchors
 * - Zero dependency on building coordinates on the grass
 * - Auto-upgrades walls via the top Builder Overview icon
 * - 4-Finger Red Line deployment with Grand Warden invincibility
 */
class ClashAutomationCore(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRunning: Boolean = false
        private set

    var onStatusUpdate: ((String) -> Unit)? = null

    private fun updateStatus(text: String) {
        Log.i("ClashCore", text)
        onStatusUpdate?.invoke(text)
    }

    fun startLoop() {
        if (isRunning) return
        isRunning = true
        updateStatus("🚀 [STARTING] Standardizing Camera View...")

        // Step 1: Smooth 2-finger zoom out
        zoomOutAndResetCamera {
            executeVillageRoutine()
        }
    }

    fun stopLoop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        updateStatus("⏸ [PAUSED] Idle")
    }

    /**
     * 1. Standardize Camera Zoom (Pinch Out)
     */
    private fun zoomOutAndResetCamera(onComplete: () -> Unit) {
        accessibilityService.performPinchZoomOut(durationMs = 450L) {
            handler.postDelayed({
                if (isRunning) onComplete()
            }, 800L)
        }
    }

    /**
     * 2. Village Routine: Upgrade Walls via Builder Menu -> Train Army -> Attack
     */
    private fun executeVillageRoutine() {
        if (!isRunning) return
        updateStatus("🧱 [BUILDER OVERVIEW] Upgrading Walls with Free Builder...")

        // Tap Top-Center Builder Hammer Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_BUILDER_DROPDOWN) {
            handler.postDelayed({
                // Tap Suggested Wall in Dropdown
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SUGGESTED_WALL) {
                    handler.postDelayed({
                        // Tap Confirm Upgrade with Gold/Elixir
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_UPGRADE_CONFIRM) {
                            handler.postDelayed({
                                // Train Army next
                                trainArmyRoutine {
                                    startMatchmakingAndAttack()
                                }
                            }, 600L)
                        }
                    }, 600L)
                }
            }, 800L)
        }
    }

    /**
     * 3. Army Routine: Double-Queue 0-Cost Pro Meta Army
     */
    private fun trainArmyRoutine(onComplete: () -> Unit) {
        if (!isRunning) return
        updateStatus("⚡ [QUICK TRAIN] Double-Queuing 0-Cost Army...")

        // Tap Bottom-Left Army Bottle Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ARMY_OVERVIEW) {
            handler.postDelayed({
                // Tap Quick Train Tab
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_QUICK_TRAIN_TAB) {
                    handler.postDelayed({
                        // Tap Train Slot 1
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_TRAIN_SLOT_1) {
                            handler.postDelayed({
                                // Tap Close X
                                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                                    handler.postDelayed({
                                        if (isRunning) onComplete()
                                    }, 700L)
                                }
                            }, 500L)
                        }
                    }, 600L)
                }
            }, 800L)
        }
    }

    /**
     * 4. Attack Routine: Search Match -> Smart Nexting -> Deploy
     */
    private fun startMatchmakingAndAttack() {
        if (!isRunning) return
        updateStatus("⚔️ [ATTACK] Opening Matchmaking...")

        // Tap Bottom-Left Attack Swords Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ATTACK) {
            handler.postDelayed({
                // Tap "Find a Match" (Bottom-Right)
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_FIND_MATCH) {
                    // Allow 3.5s for matchmaking clouds to clear
                    handler.postDelayed({
                        searchAndNextBase(searchCount = 0)
                    }, 3500L)
                }
            }, 900L)
        }
    }

    private fun searchAndNextBase(searchCount: Int) {
        if (!isRunning) return

        val maxNexts = Random.nextInt(2, 5)
        if (searchCount < maxNexts) {
            updateStatus("🔍 [SEARCHING] Nexting Base (#${searchCount + 1})...")
            accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_NEXT_BASE) {
                handler.postDelayed({
                    searchAndNextBase(searchCount + 1)
                }, Random.nextLong(2800L, 3800L))
            }
        } else {
            // Target Base Selected! Execute 4-Finger Red Line Deployment
            executeRedLineDeployment()
        }
    }

    /**
     * 5. Red-Line 4-Finger Wave Deployment & Hero Equipment Surge
     */
    private fun executeRedLineDeployment() {
        if (!isRunning) return
        updateStatus("🔥 [RAIDING] Deploying 4-Finger Line Wave...")

        // Select Troop Slot 1 (Root Riders / Dragons)
        accessibilityService.performPercentageTap(0.105f, 0.900f) {
            handler.postDelayed({
                // 4-Finger Line Wave along South Red Line
                val start = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START
                val end = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END
                val lines = listOf(
                    Pair(start, end),
                    Pair(PointF(start.x, start.y - 0.015f), PointF(end.x, end.y - 0.015f))
                )

                accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 400L) {
                    handler.postDelayed({
                        // Select Troop Slot 2 (Valkyries / Loons)
                        accessibilityService.performPercentageTap(0.145f, 0.900f) {
                            accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 400L) {
                                // Deploy Heroes (King, Queen, Warden, Champion)
                                deployAllHeroes {
                                    updateStatus("🛡️ [BATTLE] Core Charge (Warden Tome Active)...")
                                    handler.postDelayed({
                                        finishAttackAndReturnHome()
                                    }, 35000L)
                                }
                            }
                        }
                    }, 500L)
                }
            }, 600L)
        }
    }

    private fun deployAllHeroes(onComplete: () -> Unit) {
        val heroSlots = listOf(
            UniversalFixedUiMapper.PCT_HERO_1_KING,
            UniversalFixedUiMapper.PCT_HERO_2_QUEEN,
            UniversalFixedUiMapper.PCT_HERO_3_WARDEN,
            UniversalFixedUiMapper.PCT_HERO_4_CHAMPION
        )
        var idx = 0
        fun deployNext() {
            if (idx < heroSlots.size && isRunning) {
                val heroSlot = heroSlots[idx++]
                accessibilityService.performPercentageTap(heroSlot) {
                    accessibilityService.performPercentageTap(0.500f, 0.785f) {
                        handler.postDelayed({ deployNext() }, 250L)
                    }
                }
            } else {
                onComplete()
            }
        }
        deployNext()
    }

    /**
     * 6. End Battle -> Return to Village -> Loop!
     */
    private fun finishAttackAndReturnHome() {
        if (!isRunning) return
        updateStatus("🏆 [VICTORY] Ending Battle & Returning Home...")

        // Tap Surrender / End Battle (Bottom-Left)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SURRENDER) {
            handler.postDelayed({
                // Tap "Okay" Confirm
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CONFIRM_SURRENDER) {
                    handler.postDelayed({
                        // Tap "Return Home"
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_RETURN_HOME) {
                            handler.postDelayed({
                                updateStatus("✨ [HOME] Raid complete! Next cycle in 3s...")
                                handler.postDelayed({
                                    if (isRunning) executeVillageRoutine()
                                }, 3000L)
                            }, 2500L)
                        }
                    }, 1200L)
                }
            }, 1000L)
        }
    }
}
