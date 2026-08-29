package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 🎁 Merchant Daily Freebie & Magic Snack Collector
 *
 * Runs once every 24 hours:
 * 1. Opens Trader / Merchant Tent
 * 2. Claims Free Daily Potions, Magic Snacks & Glowy Ores
 * 3. Closes Tent and returns to Village
 */
class DailyFreebieCollector(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var lastCollectionTimestamp: Long = 0L

    private val PCT_MERCHANT_TENT = PointF(0.865f, 0.310f)       // Trader Tent on Village border
    private val PCT_FREE_ITEM_CARD = PointF(0.320f, 0.620f)      // Free Daily Deal Card Slot
    private val PCT_CLAIM_FREE_BTN = PointF(0.500f, 0.720f)      // "Claim Free" confirmation

    fun collectIfDue(onComplete: () -> Unit) {
        val now = System.currentTimeMillis()
        // 24 hours cooldown = 86,400,000 ms
        if (now - lastCollectionTimestamp < 86400000L) {
            onComplete()
            return
        }

        Log.i("MerchantFreebie", "=== [TRADER] Claiming Daily Free Magic Snacks & Ores ===")

        accessibilityService.performPercentageTap(PCT_MERCHANT_TENT) {
            handler.postDelayed({
                accessibilityService.performPercentageTap(PCT_FREE_ITEM_CARD) {
                    handler.postDelayed({
                        accessibilityService.performPercentageTap(PCT_CLAIM_FREE_BTN) {
                            handler.postDelayed({
                                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                                    lastCollectionTimestamp = System.currentTimeMillis()
                                    Log.i("MerchantFreebie", "✓ Daily Merchant Freebies collected successfully.")
                                    onComplete()
                                }
                            }, 600L)
                        }
                    }, 800L)
                }
            }, Random.nextLong(1200L, 1600L))
        }
    }
}
