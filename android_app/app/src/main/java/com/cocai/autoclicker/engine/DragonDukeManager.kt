package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * 🐉 5th Hero & Pet Ability Dispatcher
 *
 * Coordinates timed activation for the 5th Hero (Dragon Duke) and Pets
 * (Greedy Raven, Spirit Fox, Phoenix, Frosty, Diggy) during live attacks.
 */
class DragonDukeManager(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    val PCT_HERO_5_DRAGON_DUKE = PointF(0.420f, 0.900f) // 5th Hero Slot on Battle Ribbon
    val PCT_PET_GREEDY_RAVEN = PointF(0.480f, 0.900f)   // Pet Active Ability Trigger

    fun deployAndTrigger5thHero(dropLocation: PointF, onComplete: () -> Unit) {
        Log.i("DragonDuke", "=== [5TH HERO] Deploying Dragon Duke & Greedy Raven Pet ===")

        // Step 1: Select 5th Hero
        accessibilityService.performPercentageTap(PCT_HERO_5_DRAGON_DUKE) {
            handler.postDelayed({
                // Step 2: Drop at battle entry point
                accessibilityService.performPercentageTap(dropLocation) {
                    handler.postDelayed({
                        // Step 3: Trigger Active Pet Ability (Greedy Raven / Fox Cloak)
                        accessibilityService.performPercentageTap(PCT_PET_GREEDY_RAVEN) {
                            Log.i("DragonDuke", "✓ Dragon Duke + Greedy Raven deployed with active frenzy boost.")
                            onComplete()
                        }
                    }, 1200L)
                }
            }, 300L)
        }
    }
}
