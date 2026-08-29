package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 🏛️ Clan Capital Weekend Automation Engine
 *
 * 1. Collects free Capital Gold from the Forge
 * 2. Navigates to the Clan Capital Raid Map
 * 3. Executes District Attacks with 4-finger perimeter troop deployment
 * 4. Contributes all earned Capital Gold into highlighted clan building upgrades
 */
class ClanCapitalEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isExecutingCapital: Boolean = false
        private set

    private val PCT_FORGE = PointF(0.260f, 0.460f)                // Forge location on Village airship
    private val PCT_CLAIM_FORGE_GOLD = PointF(0.500f, 0.700f)     // Claim Forge Gold button
    private val PCT_AIRSHIP_TRAVEL = PointF(0.260f, 0.380f)       // Airship travel to Clan Capital
    private val PCT_RAID_MAP_ATTACK = PointF(0.820f, 0.780f)      // "Attack" on Capital District
    private val PCT_CONTRIBUTE_BTN = PointF(0.500f, 0.850f)       // "Contribute Capital Gold" button

    fun executeWeekendRaidRoutine(onComplete: () -> Unit) {
        if (isExecutingCapital) {
            onComplete()
            return
        }

        isExecutingCapital = true
        Log.i("ClanCapital", "=== [CLAN CAPITAL] Starting Weekend Raid & Gold Contribution ===")

        // Step 1: Collect Free Forge Capital Gold
        accessibilityService.performPercentageTap(PCT_FORGE) {
            handler.postDelayed({
                accessibilityService.performPercentageTap(PCT_CLAIM_FORGE_GOLD) {
                    handler.postDelayed({
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                            // Step 2: Travel to Clan Capital
                            travelToCapitalAndRaid(onComplete)
                        }
                    }, 800L)
                }
            }, 1200L)
        }
    }

    private fun travelToCapitalAndRaid(onComplete: () -> Unit) {
        accessibilityService.performPercentageTap(PCT_AIRSHIP_TRAVEL) {
            handler.postDelayed({
                // Step 3: Attack Capital District
                accessibilityService.performPercentageTap(PCT_RAID_MAP_ATTACK) {
                    handler.postDelayed({
                        // Deploy Capital Troops in 4-finger perimeter wave
                        val line1 = Pair(UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START, UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END)
                        accessibilityService.performPercentageMultiFingerSwipes(listOf(line1), durationMs = 400L) {
                            // Allow 25s for district attack
                            handler.postDelayed({
                                // Return to Capital Map
                                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SURRENDER) {
                                    handler.postDelayed({
                                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CONFIRM_SURRENDER) {
                                            handler.postDelayed({
                                                // Step 4: Contribute Capital Gold
                                                contributeCapitalGold {
                                                    // Return Home Village
                                                    accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_RETURN_HOME) {
                                                        isExecutingCapital = false
                                                        Log.i("ClanCapital", "✓ Clan Capital Weekend routine complete!")
                                                        onComplete()
                                                    }
                                                }
                                            }, 1500L)
                                        }
                                    }, 800L)
                                }
                            }, 25000L)
                        }
                    }, 3000L)
                }
            }, 4000L)
        }
    }

    private fun contributeCapitalGold(onFinished: () -> Unit) {
        accessibilityService.performPercentageTap(PCT_CONTRIBUTE_BTN) {
            handler.postDelayed({
                accessibilityService.performPercentageTap(0.680f, 0.650f) { // Confirm 100% contribute
                    handler.postDelayed({ onFinished() }, 800L)
                }
            }, 1200L)
        }
    }
}
