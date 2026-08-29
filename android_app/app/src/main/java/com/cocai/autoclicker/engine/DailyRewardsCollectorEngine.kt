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
     * Autonomous Daily Rewards & Trader Gift Collector
     */
    fun collectAllDailyRewards(onComplete: () -> Unit) {
        if (isCollecting) return
        isCollecting = true
        Log.i("DailyRewards", "=== [DAILY REWARDS] Claiming Free Trader Gift & Daily Ores ===")

        accessibilityService.performPercentageTap(0.850f, 0.300f) {
            handler.postDelayed({
                accessibilityService.performPercentageTap(0.300f, 0.600f) {
                    handler.postDelayed({
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                            isCollecting = false
                            Log.i("DailyRewards", "✓ Daily Trader Gift & Star Bonus Ores collected.")
                            onComplete()
                        }
                    }, 800L)
                }
            }, 1200L)
        }
    }

    /**
     * Harvest Gem Boxes & Clear Obstacles for Free Gems
     */
    fun cleanObstaclesForGems(onComplete: () -> Unit) {
        Log.i("DailyRewards", "=== [OBSTACLES] Harvesting Obstacles for Gems ===")
        val obstaclePts = listOf(
            PointF(0.350f, 0.350f),
            PointF(0.650f, 0.350f),
            PointF(0.350f, 0.650f)
        )
        var idx = 0
        fun clearNext() {
            if (idx < obstaclePts.size) {
                val pt = obstaclePts[idx++]
                accessibilityService.performPercentageTap(pt) {
                    handler.postDelayed({
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_UPGRADE_CONFIRM) {
                            handler.postDelayed({ clearNext() }, 700L)
                        }
                    }, 500L)
                }
            } else {
                onComplete()
            }
        }
        clearNext()
    }
}
