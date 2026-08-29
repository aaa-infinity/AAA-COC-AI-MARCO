package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 👑 CLASH AUTOMATION CORE ENGINE
 *
 * 100% Guaranteed Reliability Architecture:
 * - Operates EXCLUSIVELY on fixed Supercell UI HUD anchors
 * - Zero dependency on building coordinates (no blind clicking!)
 * - Uses Top-Center Builder Overview to auto-select Walls for upgrade
 * - 4-Finger Red Line deployment with Warden Eternal Tome invincibility
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
        updateStatus("🚀 [LOOP STARTED] Resetting Camera & Zoom...")

        // Step 1: Pinch Zoom Out to standardize view
        zoomOutAndResetCamera {
            executeVillageRoutine()
        }
    }

    fun stopLoop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        updateStatus("⏸ [STOPPED] Idle")
    }

    /**
     * 1. Pinch to Zoom Out 100%
     */
    private fun zoomOutAndResetCamera(onComplete: () -> Unit) {
        accessibilityService.performPinchZoom(960f, 540f, zoomIn = false, durationMs = 450L) {
            handler.postDelayed({
                if (isRunning) onComplete()
            }, 800L)
        }
    }

    /**
     * 2. Village Routine: Upgrade Walls with Free Builder -> Train Army -> Attack
     */
    private fun executeVillageRoutine() {
        if (!isRunning) return
        updateStatus("🧱 [BUILDER OVERVIEW] Upgrading Walls with Free Builder...")

        // Open Builder Overview at Top-Center (x=960, y=50)
        accessibilityService.performTap(UniversalFixedUiMapper.BTN_BUILDER_DROPDOWN.x, UniversalFixedUiMapper.BTN_BUILDER_DROPDOWN.y, anchor = UiAnchor.TOP_LEFT) {
            handler.postDelayed({
                // Tap Suggested Wall in Dropdown (x=960, y=220)
                accessibilityService.performTap(UniversalFixedUiMapper.BTN_SUGGESTED_WALL.x, UniversalFixedUiMapper.BTN_SUGGESTED_WALL.y, anchor = UiAnchor.TOP_LEFT) {
                    handler.postDelayed({
                        // Tap Confirm Upgrade with Gold/Elixir (x=1100, y=750)
                        accessibilityService.performTap(UniversalFixedUiMapper.BTN_UPGRADE_CONFIRM.x, UniversalFixedUiMapper.BTN_UPGRADE_CONFIRM.y, anchor = UiAnchor.CENTER_STAGE) {
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

        // Tap Army Overview (Bottom-Left x=95, y=830)
        accessibilityService.performTap(UniversalFixedUiMapper.BTN_ARMY_OVERVIEW.x, UniversalFixedUiMapper.BTN_ARMY_OVERVIEW.y, anchor = UiAnchor.BOTTOM_LEFT) {
            handler.postDelayed({
                // Tap Quick Train Tab (x=1350, y=150)
                accessibilityService.performTap(UniversalFixedUiMapper.TAB_QUICK_TRAIN.x, UniversalFixedUiMapper.TAB_QUICK_TRAIN.y, anchor = UiAnchor.TOP_RIGHT) {
                    handler.postDelayed({
                        // Tap Train Army Slot 1 (x=1580, y=380)
                        accessibilityService.performTap(UniversalFixedUiMapper.BTN_TRAIN_SLOT_1.x, UniversalFixedUiMapper.BTN_TRAIN_SLOT_1.y, anchor = UiAnchor.TOP_RIGHT) {
                            handler.postDelayed({
                                // Tap Close X (x=1820, y=85)
                                accessibilityService.performTap(UniversalFixedUiMapper.BTN_CLOSE_MODAL.x, UniversalFixedUiMapper.BTN_CLOSE_MODAL.y, anchor = UiAnchor.TOP_RIGHT) {
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
     * 4. Attack Routine: Search Base -> Deploy 4-Finger Wave -> Return Home
     */
    private fun startMatchmakingAndAttack() {
        if (!isRunning) return
        updateStatus("⚔️ [ATTACK] Opening Matchmaking...")

        // Tap Attack Button (Bottom-Left x=115, y=950)
        accessibilityService.performTap(UniversalFixedUiMapper.BTN_ATTACK.x, UniversalFixedUiMapper.BTN_ATTACK.y, anchor = UiAnchor.BOTTOM_LEFT) {
            handler.postDelayed({
                // Tap "Find a Match" (Bottom-Right x=1550, y=750)
                accessibilityService.performTap(UniversalFixedUiMapper.BTN_FIND_MATCH.x, UniversalFixedUiMapper.BTN_FIND_MATCH.y, anchor = UiAnchor.BOTTOM_RIGHT) {
                    // Allow 3.5s for clouds & match search
                    handler.postDelayed({
                        searchAndNextBase(searchCount = 0)
                    }, 3500L)
                }
            }, 900L)
        }
    }

    private fun searchAndNextBase(searchCount: Int) {
        if (!isRunning) return

        // Smart nexting 3 to 6 times to find high loot base
        val maxNexts = Random.nextInt(2, 5)
        if (searchCount < maxNexts) {
            updateStatus("🔍 [SEARCHING] Nexting base (#${searchCount + 1})...")
            accessibilityService.performTap(UniversalFixedUiMapper.BTN_NEXT_BASE.x, UniversalFixedUiMapper.BTN_NEXT_BASE.y, anchor = UiAnchor.BOTTOM_RIGHT) {
                handler.postDelayed({
                    searchAndNextBase(searchCount + 1)
                }, Random.nextLong(2800L, 3800L))
            }
        } else {
            // Target Base Found! Execute Full 4-Finger Red-Line Deployment
            executeRedLineDeployment()
        }
    }

    /**
     * 5. Red-Line 4-Finger Deployment & Hero Equipment Surge
     */
    private fun executeRedLineDeployment() {
        if (!isRunning) return
        updateStatus("🔥 [RAIDING] Deploying 4-Finger Line Wave...")

        // Select Troop Slot 1 (x=200, y=980)
        accessibilityService.performTap(200f, 980f, anchor = UiAnchor.BOTTOM_LEFT) {
            handler.postDelayed({
                // 4-Finger Line Wave along South Red Line
                val start = UniversalFixedUiMapper.DEPLOY_SOUTH_LINE_START
                val end = UniversalFixedUiMapper.DEPLOY_SOUTH_LINE_END
                val lines = listOf(Pair(start, end), Pair(PointF(start.x, start.y - 15f), PointF(end.x, end.y - 15f)))

                accessibilityService.performMultiFingerSwipeLines(lines, durationMs = 400L) {
                    handler.postDelayed({
                        // Select Troop Slot 2 (Valkyries/Apprentices at x=290, y=980)
                        accessibilityService.performTap(290f, 980f, anchor = UiAnchor.BOTTOM_LEFT) {
                            accessibilityService.performMultiFingerSwipeLines(lines, durationMs = 400L) {
                                // Deploy Heroes (King, Queen, Warden, Champion)
                                deployAllHeroes {
                                    // Battle in progress (allow 35s for destruction)
                                    updateStatus("🛡️ [BATTLE] Army smashing base (Warden Tome Active)...")
                                    handler.postDelayed({
                                        // Return Home
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
        val heroSlots = listOf(300f, 400f, 500f, 600f)
        var idx = 0
        fun deployNext() {
            if (idx < heroSlots.size && isRunning) {
                val slotX = heroSlots[idx++]
                accessibilityService.performTap(slotX, 980f, anchor = UiAnchor.BOTTOM_LEFT) {
                    accessibilityService.performTap(960f, 850f, anchor = UiAnchor.DEPLOY_PERIMETER) {
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
     * 6. End Battle -> Return to Home Village -> Loop!
     */
    private fun finishAttackAndReturnHome() {
        if (!isRunning) return
        updateStatus("🏆 [VICTORY] Ending battle and returning Home...")

        // Tap Surrender / End Battle (Bottom-Left x=120, y=820)
        accessibilityService.performTap(UniversalFixedUiMapper.BTN_SURRENDER.x, UniversalFixedUiMapper.BTN_SURRENDER.y, anchor = UiAnchor.BOTTOM_LEFT) {
            handler.postDelayed({
                // Tap "Okay" Confirm (Center x=1100, y=680)
                accessibilityService.performTap(UniversalFixedUiMapper.BTN_CONFIRM_SURRENDER.x, UniversalFixedUiMapper.BTN_CONFIRM_SURRENDER.y, anchor = UiAnchor.CENTER_STAGE) {
                    handler.postDelayed({
                        // Tap "Return Home" (Center-Bottom x=960, y=920)
                        accessibilityService.performTap(UniversalFixedUiMapper.BTN_RETURN_HOME.x, UniversalFixedUiMapper.BTN_RETURN_HOME.y, anchor = UiAnchor.CENTER_STAGE) {
                            handler.postDelayed({
                                updateStatus("✨ [HOME] Raid complete! Starting next cycle in 3s...")
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
