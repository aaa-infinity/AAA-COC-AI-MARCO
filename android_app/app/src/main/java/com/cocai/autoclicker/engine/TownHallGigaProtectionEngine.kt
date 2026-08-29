package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

class TownHallGigaProtectionEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isGigaTriggered: Boolean = false
        private set

    /**
     * Executes Core Giga Poison Protection Sequence:
     * - Freezes Town Hall / Monolith
     * - Triggers Grand Warden Eternal Tome invulnerability
     * - Triggers Barbarian King Giant Gauntlet quake
     */
    fun protectArmyFromGigaExplosion(
        townHallLocation: PointF = PointF(960f, 540f),
        onProtected: () -> Unit
    ) {
        if (isGigaTriggered) return
        isGigaTriggered = true
        Log.i("GigaProtection", "🛡️ [COMPLEX CORE DEFENSE] Army entered Town Hall Giga Inferno Zone! Activating invulnerability...")

        // Step 1: Cast Freeze on Town Hall (Slot 6: x=720, y=980)
        accessibilityService.performTap(720f, 980f) {
            accessibilityService.performTap(townHallLocation.x, townHallLocation.y) {
                // Step 2: Trigger Grand Warden Eternal Tome (Hero Slot 3: x=500, y=980)
                handler.postDelayed({
                    Log.i("GigaProtection", "✨ Grand Warden Eternal Tome ACTIVATED: 100% Army Invulnerability Active!")
                    accessibilityService.performTap(500f, 980f) {
                        // Step 3: Trigger King Giant Gauntlet (Hero Slot 1: x=300, y=980)
                        handler.postDelayed({
                            accessibilityService.performTap(300f, 980f) {
                                isGigaTriggered = false
                                onProtected()
                            }
                        }, 300L)
                    }
                }, 400L)
            }
        }
    }
}
