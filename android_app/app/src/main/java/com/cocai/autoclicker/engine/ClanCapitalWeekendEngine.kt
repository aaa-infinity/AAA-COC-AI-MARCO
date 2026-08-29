package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class ClanCapitalWeekendEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    private val multiTouch = MultiTouchDeployer(accessibilityService)
    var isCapitalRaidActive: Boolean = false
        private set

    /**
     * Autonomous Clan Capital Raid Weekend Loop:
     * 1. Opens Capital Airship
     * 2. Executes Capital District Raid with Multi-Touch
     * 3. Contributes Capital Gold to clan projects
     * 4. Returns Home
     */
    fun performCapitalRaidIfActive(onComplete: () -> Unit) {
        if (isCapitalRaidActive) return
        isCapitalRaidActive = true
        Log.i("ClanCapital", "=== [CLAN CAPITAL] Checking Raid Weekend Airship ===")

        // Step 1: Tap Capital Airship on Home Village beach (x=1600, y=250)
        accessibilityService.performTap(1600f, 250f) {
            handler.postDelayed({
                // Step 2: Tap Attack District Button (x=1450, y=850)
                accessibilityService.performTap(1450f, 850f) {
                    handler.postDelayed({
                        // Step 3: 4-Finger Multi-Touch Super Miner & Frost Spell deployment
                        accessibilityService.performTap(200f, 980f) // Troop Slot 1
                        multiTouch.deployFourFingerWave(
                            startCorner = PointF(600f, 820f),
                            endCorner = PointF(1350f, 820f),
                            waves = 3
                        ) {
                            handler.postDelayed({
                                // Cast Frost/Heal Spell (x=700, y=980)
                                accessibilityService.performTap(700f, 980f)
                                accessibilityService.performTap(960f, 540f)

                                // Wait for Capital Destruction & Return
                                handler.postDelayed({
                                    // Step 4: Return to Capital Map (x=960, y=920)
                                    accessibilityService.performTap(960f, 920f) {
                                        handler.postDelayed({
                                            // Step 5: Tap Contribute Capital Gold (x=960, y=850)
                                            accessibilityService.performTap(960f, 850f) {
                                                handler.postDelayed({
                                                    // Step 6: Return Home Village (x=120, y=880)
                                                    accessibilityService.performTap(120f, 880f) {
                                                        isCapitalRaidActive = false
                                                        Log.i("ClanCapital", "✓ Clan Capital raid & gold contribution complete.")
                                                        onComplete()
                                                    }
                                                }, Random.nextLong(600L, 900L))
                                            }
                                        }, Random.nextLong(800L, 1200L))
                                    }
                                }, 30000L)
                            }, Random.nextLong(1000L, 1500L))
                        }
                    }, Random.nextLong(1500L, 2000L))
                }
            }, Random.nextLong(1500L, 2000L))
        }
    }
}
