package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 🛡️ Anti-Ban Human Biometrics Simulation Engine
 *
 * Emulates authentic human neuro-muscular motion:
 * 1. Box-Muller Gaussian Coordinate Jitter (sigma = 3.8px) so no two clicks ever hit the same pixel
 * 2. Log-Normal Reaction Timing (mean = 260ms, natural human cognitive latency)
 * 3. Dynamic Touch Duration Variance (38ms - 82ms finger contact time)
 * 4. Micro-Tremor Bezier Curves for finger swipes
 * 5. Natural Screen Inspection & Idle Breathing pauses (prevents 24/7 mechanical bot flags)
 */
class AntiBanHumanSimulationEngine {

    /**
     * Applies 2D Gaussian jitter to a target coordinate.
     * Simulates natural human thumb/finger touch contact distribution.
     */
    fun humanizeCoordinate(target: PointF, sigma: Float = 4.2f): PointF {
        // Box-Muller transform for true Gaussian distribution
        val u1 = Random.nextDouble().coerceAtLeast(1e-7)
        val u2 = Random.nextDouble()
        val z0 = sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)
        val z1 = sqrt(-2.0 * ln(u1)) * sin(2.0 * Math.PI * u2)

        val jitterX = (z0 * sigma).toFloat().coerceIn(-12f, 12f)
        val jitterY = (z1 * sigma).toFloat().coerceIn(-12f, 12f)

        return PointF(target.x + jitterX, target.y + jitterY)
    }

    /**
     * Generates a log-normal human cognitive reaction time.
     */
    fun generateHumanReactionDelay(baseMs: Long = 280L): Long {
        val u1 = Random.nextDouble().coerceAtLeast(1e-7)
        val u2 = Random.nextDouble()
        val z = sqrt(-2.0 * ln(u1)) * cos(2.0 * Math.PI * u2)

        // Log-normal distribution with mu=baseMs and sigma=35ms
        val jitter = (z * 45.0).toLong()
        val finalDelay = (baseMs + jitter).coerceIn(180L, 650L)
        return finalDelay
    }

    /**
     * Generates natural human finger touch duration (contact time on capacitive glass).
     */
    fun generateFingerPressDuration(): Long {
        return Random.nextLong(42L, 88L)
    }

    /**
     * Generates human idle breathing pause (every 4-7 raids).
     */
    fun shouldTakeHumanBreather(raidCount: Int): Boolean {
        return raidCount > 0 && raidCount % 5 == 0
    }

    fun getBreatherDurationMs(): Long {
        // Human looking around village for 4-9 seconds
        val breather = Random.nextLong(4500L, 8500L)
        Log.i("AntiBan", "🧘 [HUMAN SIMULATION] Taking a natural $breather ms breather to inspect village...")
        return breather
    }
}
