package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class HeroEquipmentAutoTriggerEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    private val heroSlots = listOf(
        PointF(300f, 980f), // Slot 1: Barbarian King (Giant Gauntlet / Spiky Ball)
        PointF(400f, 980f), // Slot 2: Archer Queen (Invisibility Vial / Frozen Arrow)
        PointF(500f, 980f), // Slot 3: Grand Warden (Eternal Tome / Healing Tome)
        PointF(600f, 980f)  // Slot 4: Royal Champion (Seeking Shield / Rocket Spear)
    )

    /**
     * Executes timed tactical activation of active hero equipment combos
     * during the battle's climax.
     */
    fun triggerHeroEquipmentSequence(onComplete: () -> Unit) {
        Log.i("HeroEquip", "⚔️ [HERO EQUIPMENT ENGINE] Activating Pro Equipment Combos (Giant Gauntlet, Invisibility, Eternal Tome)...")

        // Step 1: Warden Eternal Tome (Invincibility shield)
        accessibilityService.performTap(heroSlots[2].x, heroSlots[2].y) {
            handler.postDelayed({
                // Step 2: King Giant Gauntlet (Rage & Earthquake smash)
                accessibilityService.performTap(heroSlots[0].x, heroSlots[0].y) {
                    handler.postDelayed({
                        // Step 3: Queen Invisibility Vial (High DPS stealth)
                        accessibilityService.performTap(heroSlots[1].x, heroSlots[1].y) {
                            handler.postDelayed({
                                // Step 4: Royal Champion Rocket Spear (Long-range core snipe)
                                accessibilityService.performTap(heroSlots[3].x, heroSlots[3].y) {
                                    Log.i("HeroEquip", "✓ All 4 Hero Equipment Abilities successfully triggered!")
                                    onComplete()
                                }
                            }, Random.nextLong(1200L, 2000L))
                        }
                    }, Random.nextLong(1500L, 2500L))
                }
            }, Random.nextLong(800L, 1500L))
        }
    }
}
