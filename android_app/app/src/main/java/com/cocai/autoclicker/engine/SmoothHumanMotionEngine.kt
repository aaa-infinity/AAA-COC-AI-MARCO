package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log
import kotlin.math.pow
import kotlin.random.Random

class SmoothHumanMotionEngine {

    /**
     * Generates a natural human Bezier curve with ease-in / ease-out velocity
     */
    fun generateSmoothBezierPath(
        start: PointF,
        end: PointF,
        steps: Int = 12
    ): List<PointF> {
        val controlX = (start.x + end.x) / 2f + Random.nextDouble(-30.0, 30.0).toFloat()
        val controlY = (start.y + end.y) / 2f + Random.nextDouble(-30.0, 30.0).toFloat()

        val points = mutableListOf<PointF>()
        for (i in 0..steps) {
            val t = i.toFloat() / steps.toFloat()
            // Ease-in ease-out cubic smoothing
            val smoothedT = if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).pow(2) / 2f

            val x = (1 - smoothedT).pow(2) * start.x + 2 * (1 - smoothedT) * smoothedT * controlX + smoothedT.pow(2) * end.x
            val y = (1 - smoothedT).pow(2) * start.y + 2 * (1 - smoothedT) * smoothedT * controlY + smoothedT.pow(2) * end.y

            points.add(PointF(x, y))
        }
        return points
    }

    /**
     * Calculates human reaction delay using Log-Normal distribution
     * (mimics human neural reaction times around ~220ms - 380ms)
     */
    fun getHumanReactionDelay(baseMs: Long = 240L, maxExtraMs: Long = 180L): Long {
        val jitter = (Random.nextDouble().pow(1.5) * maxExtraMs).toLong()
        return baseMs + jitter
    }
}
