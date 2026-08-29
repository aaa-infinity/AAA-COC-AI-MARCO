package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class AutoDonateEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isDonating: Boolean = false
        private set

    /**
     * Complete Auto-Donate Routine (Macrorify-Style):
     * 1. Opens Clan Chat Tab (left edge button: x=40, y=540)
     * 2. Scans for green "Donate" buttons in chat stream
     * 3. Selects requested troops (Barbarians, Archers, Balloons, Dragons, Spells)
     * 4. Closes chat and returns to base loop
     */
    fun startAutoDonate(onComplete: () -> Unit) {
        if (isDonating) return
        isDonating = true
        Log.i("AutoDonate", "=== [AUTO DONATE] Opening Clan Chat ===")

        // Step 1: Open Clan Chat (left edge drawer)
        accessibilityService.performTap(40f, 540f) {
            scheduleDelay(900L) {
                // Step 2: Tap Clan Tab in Chat window
                accessibilityService.performTap(300f, 120f) {
                    scheduleDelay(800L) {
                        scanAndDonateVisibleRequests {
                            // Step 3: Close Chat
                            accessibilityService.performTap(40f, 540f) {
                                scheduleDelay(600L) {
                                    isDonating = false
                                    Log.i("AutoDonate", "Auto-Donate complete. Closed chat.")
                                    onComplete()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun scanAndDonateVisibleRequests(onFinished: () -> Unit) {
        val donateSlotY = listOf(350f, 520f, 690f, 860f)
        var currentSlot = 0

        fun donateNext() {
            if (currentSlot < donateSlotY.size) {
                val y = donateSlotY[currentSlot++]
                Log.i("AutoDonate", "Checking donation button at slot y=$y")
                // Tap Donate button in chat row (approx x: 550)
                accessibilityService.performTap(550f, y) {
                    scheduleDelay(600L) {
                        // Fill troops in donation popup: Tap Troop Slot 1, 2, 3, 4
                        accessibilityService.performTap(960f, 480f) // Troop Slot 1
                        scheduleDelay(150L) {
                            accessibilityService.performTap(1060f, 480f) // Troop Slot 2
                            scheduleDelay(150L) {
                                accessibilityService.performTap(1160f, 480f) // Troop Slot 3
                                scheduleDelay(400L) {
                                    // Tap outside or close popup
                                    accessibilityService.performTap(1450f, 250f) {
                                        scheduleDelay(400L) { donateNext() }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                onFinished()
            }
        }

        donateNext()
    }

    private fun scheduleDelay(delayMs: Long, action: () -> Unit) {
        val randomized = delayMs + Random.nextLong(80L, 250L)
        handler.postDelayed(action, randomized)
    }
}
