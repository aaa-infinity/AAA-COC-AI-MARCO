package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class SeasonPassCollectorEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isClaiming: Boolean = false
        private set

    /**
     * Autonomous Season Pass & Challenges Claimer:
     * - Opens Season Challenges icon (bottom-left x=140, y=720)
     * - Taps "Claim All" rewards
     * - Closes window
     */
    fun claimSeasonPassRewards(onComplete: () -> Unit) {
        if (isClaiming) return
        isClaiming = true
        Log.i("SeasonPass", "=== [SEASON PASS] Checking unlocked pass tiers & free magic items ===")

        // Step 1: Open Season Pass (x=140, y=720)
        accessibilityService.performTap(140f, 720f) {
            handler.postDelayed({
                // Step 2: Tap Rewards Tab (x=960, y=150)
                accessibilityService.performTap(960f, 150f) {
                    handler.postDelayed({
                        // Step 3: Tap Claim Buttons across tier path
                        accessibilityService.performTap(650f, 750f) {
                            handler.postDelayed({
                                accessibilityService.performTap(950f, 750f) {
                                    handler.postDelayed({
                                        accessibilityService.performTap(1250f, 750f) {
                                            handler.postDelayed({
                                                // Close Window
                                                accessibilityService.performTap(1820f, 85f) {
                                                    isClaiming = false
                                                    Log.i("SeasonPass", "✓ Season Pass rewards claimed.")
                                                    onComplete()
                                                }
                                            }, Random.nextLong(400L, 700L))
                                        }
                                    }, Random.nextLong(400L, 700L))
                                }
                            }, Random.nextLong(400L, 700L))
                        }
                    }, Random.nextLong(700L, 1000L))
                }
            }, Random.nextLong(800L, 1200L))
        }
    }
}
