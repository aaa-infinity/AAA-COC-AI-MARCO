package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class BuilderBaseEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    private val multiTouch = MultiTouchDeployer(accessibilityService)

    var isRunning: Boolean = false
        private set

    /**
     * Executes Builder Base 2.0 Fast Farming Loop:
     * 1. Travel via Boat (x=1600, y=300)
     * 2. Collect Builder Gold & Elixir
     * 3. Start Instant Battle
     * 4. Multi-Touch 4-Finger Drop
     * 5. Surrender/Exit for Instant 3-Star Loot & Return to Home Village
     */
    fun startBuilderBaseLoop(onComplete: () -> Unit) {
        if (isRunning) return
        isRunning = true
        Log.i("BuilderBase", "=== [BUILDER BASE 2.0] Sailing to Builder Base ===")

        // Step 1: Tap Boat
        accessibilityService.performTap(1600f, 300f) {
            scheduleDelay(2500L) {
                // Step 2: Collect BB Resources
                collectBuilderResources {
                    // Step 3: Tap Attack Button (x=120, y=950)
                    accessibilityService.performTap(120f, 950f) {
                        scheduleDelay(1200L) {
                            // Tap "Find Match" (x=1450f, y=650f)
                            accessibilityService.performTap(1450f, 650f) {
                                scheduleDelay(3500L) {
                                    // Step 4: Multi-Touch Baby Dragon / PEKKA Drop
                                    deployBuilderArmy {
                                        scheduleDelay(8000L) {
                                            // Step 5: Return Home via Boat
                                            returnToHomeVillage {
                                                isRunning = false
                                                Log.i("BuilderBase", "Builder Base routine complete.")
                                                onComplete()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun collectBuilderResources(onComplete: () -> Unit) {
        val pts = listOf(
            PointF(850f, 450f), // Builder Gold Mine
            PointF(1050f, 480f), // Builder Elixir Collector
            PointF(960f, 540f)  // Gem Mine
        )
        var idx = 0
        fun next() {
            if (idx < pts.size) {
                val p = pts[idx++]
                accessibilityService.performTap(p.x, p.y) {
                    scheduleDelay(350L) { next() }
                }
            } else {
                onComplete()
            }
        }
        next()
    }

    private fun deployBuilderArmy(onComplete: () -> Unit) {
        Log.i("BuilderBase", "Executing Builder Base 4-Finger Multi-Touch Drop...")
        // Select Slot 1 & Drop
        accessibilityService.performTap(200f, 980f)
        multiTouch.deployFourFingerWave(
            startCorner = PointF(600f, 800f),
            endCorner = PointF(1350f, 800f),
            waves = 2
        ) {
            // Select Hero / Battle Copter & Drop
            accessibilityService.performTap(300f, 980f)
            accessibilityService.performTap(960f, 830f)
            onComplete()
        }
    }

    private fun returnToHomeVillage(onComplete: () -> Unit) {
        // Tap Surrender / End Battle
        accessibilityService.performTap(120f, 880f) {
            scheduleDelay(800L) {
                accessibilityService.performTap(1100f, 680f) { // Confirm
                    scheduleDelay(2000L) {
                        // Tap Boat back to Home Village (x=300, y=850)
                        accessibilityService.performTap(300f, 850f) {
                            scheduleDelay(2500L, onComplete)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleDelay(delayMs: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMs + Random.nextLong(80L, 200L))
    }
}
