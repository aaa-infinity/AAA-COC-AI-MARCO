package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.math.hypot

data class ActiveSpellZone(
    val type: String, // "RAGE", "FREEZE", "OVERGROWTH", "POISON"
    val center: PointF,
    val radiusPx: Float,
    val expirationTimestamp: Long
)

class ComplexBaseGeometryEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val activeSpells = mutableListOf<ActiveSpellZone>()

    /**
     * Defensive Clan Castle Counter:
     * Drops Poison & Freeze on emerging enemy CC troops (Super Minions, E-Drags, Lava Hounds)
     */
    fun counterDefendingClanCastle(enemyCcLocation: PointF, onComplete: () -> Unit) {
        Log.i("BaseGeometry", "🚨 Enemy Defending Clan Castle emerged at (${enemyCcLocation.x}, ${enemyCcLocation.y})! Deploying Poison & Freeze counter...")
        
        // Slot 7: Poison Spell (x=820, y=980)
        accessibilityService.performTap(820f, 980f) {
            accessibilityService.performTap(enemyCcLocation.x, enemyCcLocation.y) {
                // Slot 6: Freeze Spell (x=720, y=980)
                accessibilityService.performTap(720f, 980f) {
                    accessibilityService.performTap(enemyCcLocation.x, enemyCcLocation.y) {
                        Log.i("BaseGeometry", "✓ Defending Clan Castle neutralized with Poison/Freeze.")
                        onComplete()
                    }
                }
            }
        }
    }

    /**
     * Prevents duplicate spell casting within an active spell radius
     */
    fun canDropSpellAt(target: PointF, type: String, radiusPx: Float = 160f): Boolean {
        val now = System.currentTimeMillis()
        activeSpells.removeAll { it.expirationTimestamp <= now }

        val overlaps = activeSpells.any {
            it.type == type && hypot(it.center.x - target.x, it.center.y - target.y) < (radiusPx * 0.75f)
        }

        if (!overlaps) {
            val durationMs = when (type) {
                "RAGE" -> 18000L
                "FREEZE" -> 6500L
                "OVERGROWTH" -> 28000L
                else -> 12000L
            }
            activeSpells.add(ActiveSpellZone(type, target, radiusPx, now + durationMs))
            return true
        }

        Log.d("BaseGeometry", "Skipping $type drop at $target: Existing active spell zone.")
        return false
    }
}
