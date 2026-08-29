package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class DailyRewardsCollectorEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isCollecting: Boolean = false
        private set

    /**
     * Autonomous Daily Rewards & Trader Gift Collector:
     * 1. Opens Daily Trader Tent
     * 2. Claims Free Daily Gift
     * 3. Collects Star Bonus Ores
     */
    fun collectAllDailyRewards(onComplete: () -> Unit) {
        if (isCollecting) return
        isCollecting = true
        Log.i("DailyRewards", "=== [DAILY REWARDS] Claiming Free Trader Gift & Daily Ores ===")

        // Step 1: Tap Daily Trader Tent (x=1650, y=320)
        accessibilityService.performTap(1650f, 320f) {
            handler.postDelayed({
                // Step 2: Tap Free Gift Claim Button (x=550, y=650)
                accessibilityService.performTap(550f, 650f) {
                    handler.postDelayed({
                        // Step 3: Close Trader Tent (x=1820, y=85)
                        accessibilityService.performTap(1820f, 85f) {
                            handler.postDelayed({
                                // Step 4: Tap Treasury / Star Bonus Cart (x=1600, y=900)
                                accessibilityService.performTap(1600f, 900f) {
                                    handler.postDelayed({
                                        accessibilityService.performTap(1100f, 750f) {
                                            isCollecting = false
                                            Log.i("DailyRewards", "✓ Daily Trader Gift & Star Bonus Ores collected.")
                                            onComplete()
                                        }
                                    }, Random.nextLong(600L, 900L))
                                }
                            }, Random.nextLong(600L, 900L))
                        }
                    }, Random.nextLong(700L, 1000L))
                }
            }, Random.nextLong(1200L, 1600L))
        }
    }
}
