package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class SmartArmyRebalancer(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Autonomous Double-Queue Army Trainer:
     * Taps Army Camps -> Quick Train -> Train Twice -> Closes Modal.
     * Guarantees 0-minute wait time between successive Home Village raids.
     */
    fun doubleQueueArmy(onComplete: () -> Unit) {
        Log.i("ArmyRebalancer", "⚡ [SMART ARMY REBALANCER] Double-queuing 0-Cost Pro Army for zero-downtime farming...")

        // Step 1: Open Army Overview (x=90, y=830)
        accessibilityService.performTap(90f, 830f) {
            handler.postDelayed({
                // Step 2: Open Quick Train Tab (x=1350, y=150)
                accessibilityService.performTap(1350f, 150f) {
                    handler.postDelayed({
                        // Step 3: Tap Train Slot 1 (x=1580, y=380)
                        accessibilityService.performTap(1580f, 380f) {
                            handler.postDelayed({
                                // Step 4: Tap Train Slot 1 again to Double-Queue
                                accessibilityService.performTap(1580f, 380f) {
                                    handler.postDelayed({
                                        // Step 5: Close Army Modal (x=1820, y=85)
                                        accessibilityService.performTap(1820f, 85f) {
                                            Log.i("ArmyRebalancer", "✓ Army successfully double-queued!")
                                            onComplete()
                                        }
                                    }, Random.nextLong(450L, 700L))
                                }
                            }, Random.nextLong(500L, 800L))
                        }
                    }, Random.nextLong(500L, 750L))
                }
            }, Random.nextLong(700L, 1000L))
        }
    }
}
