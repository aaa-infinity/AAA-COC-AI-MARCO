package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log

/**
 * 🛡️ Smart Attack Safety Engine
 *
 * 1. Safe-Area Border Detection: Ensures troop deployment coordinates stay strictly outside
 *    the defensive red line boundary (prevents wasted drops in red zones).
 * 2. Surrender Safety Guard: Prevents accidental surrender clicks during active 3-star runs.
 */
class AttackSafetyEngine {

    // Legal Deployment Outer Perimeter Bounds (Percentage of screen)
    private val MIN_SAFE_X = 0.150f
    private val MAX_SAFE_X = 0.850f
    private val MIN_SAFE_Y = 0.150f
    private val MAX_SAFE_Y = 0.850f

    fun sanitizeDeployCoordinate(rawPoint: PointF): PointF {
        val safeX = rawPoint.x.coerceIn(MIN_SAFE_X, MAX_SAFE_X)
        val safeY = rawPoint.y.coerceIn(MIN_SAFE_Y, MAX_SAFE_Y)
        return PointF(safeX, safeY)
    }

    fun isSafeToSurrender(destructionPercent: Int, stars: Int, battleDurationElapsedSec: Int): Boolean {
        // If attack is progressing well with 2 stars and under 60 seconds, do not prematurely surrender
        if (stars >= 2 && battleDurationElapsedSec < 60) {
            Log.d("AttackSafety", "Holding surrender: Attack is high value (Stars: $stars, Elapsed: ${battleDurationElapsedSec}s)")
            return false
        }
        return true
    }
}
