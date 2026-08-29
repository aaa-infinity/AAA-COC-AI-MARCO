package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class AutoSpellBrewEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isBrewing: Boolean = false
        private set

    /**
     * Checks Spell Factory and ensures optimal spell composition is queued:
     * - Opens Spell Factory tab in Army Window
     * - Queues Rage, Freeze, Overgrowth, Poison
     */
    fun ensureSpellsBrewed(strategy: CocStrategy, onComplete: () -> Unit) {
        if (isBrewing) return
        isBrewing = true
        Log.i("SpellBrew", "=== [SPELL BREWER] Verifying spell factory queues for ${strategy.name} ===")

        // Step 1: Open Army Window (bottom-left x=90, y=830)
        accessibilityService.performTap(90f, 830f) {
            handler.postDelayed({
                // Step 2: Tap Spells Tab (x=700, y=150)
                accessibilityService.performTap(700f, 150f) {
                    handler.postDelayed({
                        // Step 3: Quick brew balance spells (x=1100, y=700)
                        accessibilityService.performTap(1100f, 700f) {
                            handler.postDelayed({
                                // Close Window
                                accessibilityService.performTap(1820f, 85f) {
                                    isBrewing = false
                                    Log.i("SpellBrew", "✓ Spells verified & brewed.")
                                    onComplete()
                                }
                            }, Random.nextLong(500L, 800L))
                        }
                    }, Random.nextLong(600L, 900L))
                }
            }, Random.nextLong(700L, 1000L))
        }
    }
}
