package com.cocai.autoclicker.engine

import android.graphics.PointF
import java.util.Random

/**
 * 🎯 Dynamic Gaussian Jitter & Humanized Motion Calibrator
 *
 * Adds micro-dispersion (±6px to ±14px) and organic pacing variance (1.2s ± 0.35s)
 * so touches mimic real human muscle mechanics.
 */
class GaussianMotionCalibrator {

    private val random = Random()

    fun applyGaussianJitter(point: PointF, screenWidth: Int, screenHeight: Int): PointF {
        // Gaussian distribution (mean = 0, stdDev = 4px) capped at ±14px
        val jitterX = ((random.nextGaussian() * 4.0).coerceIn(-14.0, 14.0)).toFloat() / screenWidth.toFloat()
        val jitterY = ((random.nextGaussian() * 4.0).coerceIn(-14.0, 14.0)).toFloat() / screenHeight.toFloat()

        return PointF(
            (point.x + jitterX).coerceIn(0.01f, 0.99f),
            (point.y + jitterY).coerceIn(0.01f, 0.99f)
        )
    }

    fun getHumanizedDelayMs(baseDelayMs: Long = 1200L): Long {
        // Base delay ± 350ms with Gaussian curve
        val variance = (random.nextGaussian() * 150.0).toLong().coerceIn(-350L, 350L)
        return (baseDelayMs + variance).coerceAtLeast(300L)
    }
}
