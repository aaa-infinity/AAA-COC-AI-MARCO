package com.cocai.autoclicker.engine

import android.graphics.PointF
import java.util.Random

class GaussianMotionCalibrator {
    private val random = Random()

    fun applyJitter(point: PointF, maxDeltaPct: Float = 0.008f): PointF {
        val dx = (random.nextGaussian() * (maxDeltaPct / 2.5)).toFloat().coerceIn(-maxDeltaPct, maxDeltaPct)
        val dy = (random.nextGaussian() * (maxDeltaPct / 2.5)).toFloat().coerceIn(-maxDeltaPct, maxDeltaPct)
        return PointF((point.x + dx).coerceIn(0.02f, 0.98f), (point.y + dy).coerceIn(0.02f, 0.98f))
    }

    fun humanDelay(baseMs: Long, varianceMs: Long = 200L): Long {
        val jitter = (random.nextGaussian() * (varianceMs / 2.0)).toLong()
        return (baseMs + jitter).coerceAtLeast(50L)
    }
}
