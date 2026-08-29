package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * 🐉 Army Readiness & 0-Cost Quick Train Engine
 *
 * Checks if the Dragon army is 100% full and ready for battle,
 * and double-queues Quick Train Slot 1 (0-Cost Zap Dragons).
 */
class ArmyReadinessEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Ensures Quick Train Slot 1 (0-cost Zap Dragons) is double-queued
     */
    fun ensureArmyTrainedAndReady(onReady: () -> Unit) {
        Log.i("ArmyReadiness", "🐉 Checking army readiness and queuing Quick Train Slot 1...")

        // Step 1: Open Army Overview (Bottom-Left Army Bottle Icon: x=0.055, y=0.900)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ARMY_OVERVIEW) {
            handler.postDelayed({
                // Step 2: Tap "Quick Train" Tab (x=0.680, y=0.150)
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_QUICK_TRAIN_TAB) {
                    handler.postDelayed({
                        // Step 3: Tap "Train" for Slot 1 (Zap Dragons: x=0.850, y=0.320)
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_TRAIN_SLOT_1) {
                            handler.postDelayed({
                                // Step 4: Tap "Train" for Slot 1 again to double-queue
                                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_TRAIN_SLOT_1) {
                                    handler.postDelayed({
                                        // Step 5: Close Modal (Top-Right X: x=0.930, y=0.110)
                                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                                            handler.postDelayed({
                                                Log.i("ArmyReadiness", "✓ Army double-queued and ready for combat")
                                                onReady()
                                            }, 600L)
                                        }
                                    }, 400L)
                                }
                            }, 450L)
                        }
                    }, 500L)
                }
            }, 700L)
        }
    }
}
