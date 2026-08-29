package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class MultiTouchDeployer(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    /**
     * 4-Finger Simultaneous Line Deployment:
     * Deploys an entire army wave (Dragons, Root Riders, Balloons) across the red deployment border
     * using 4 fingers simultaneously!
     */
    fun deployFourFingerWave(
        startCorner: PointF,
        endCorner: PointF,
        waves: Int = 3,
        onComplete: () -> Unit
    ) {
        Log.i("MultiTouch", "Executing 4-Finger Simultaneous Wave Deployment ($waves waves)...")
        var currentWave = 0

        fun runNextWave() {
            if (currentWave < waves) {
                currentWave++
                val dx = (endCorner.x - startCorner.x) / 3f
                val dy = (endCorner.y - startCorner.y) / 3f

                val points = (0..3).map { i ->
                    PointF(
                        startCorner.x + dx * i + Random.nextDouble(-12.0, 12.0).toFloat(),
                        startCorner.y + dy * i + Random.nextDouble(-12.0, 12.0).toFloat()
                    )
                }

                accessibilityService.performMultiTouchTaps(points, durationMs = 70L) {
                    handler.postDelayed({ runNextWave() }, Random.nextLong(180L, 260L))
                }
            } else {
                Log.i("MultiTouch", "4-Finger Wave Deployment complete.")
                onComplete()
            }
        }

        runNextWave()
    }

    /**
     * 2-Finger Simultaneous Corner Funnel:
     * Drops funnel units (e.g. King on Left, Queen on Right) at the exact same millisecond.
     */
    fun deployTwoFingerFunnel(
        leftCorner: PointF,
        rightCorner: PointF,
        taps: Int = 2,
        onComplete: () -> Unit
    ) {
        Log.i("MultiTouch", "Executing 2-Finger Simultaneous Funnel...")
        var count = 0

        fun tapBoth() {
            if (count < taps) {
                count++
                val points = listOf(
                    PointF(leftCorner.x + Random.nextInt(-8, 8), leftCorner.y + Random.nextInt(-8, 8)),
                    PointF(rightCorner.x + Random.nextInt(-8, 8), rightCorner.y + Random.nextInt(-8, 8))
                )
                accessibilityService.performMultiTouchTaps(points, durationMs = 60L) {
                    handler.postDelayed({ tapBoth() }, Random.nextLong(150L, 220L))
                }
            } else {
                onComplete()
            }
        }

        tapBoth()
    }

    /**
     * 4-Finger Multi-Line Simultaneous Swipe Drop:
     * 4 fingers dragging along different segments of the perimeter simultaneously.
     */
    fun deployFourFingerMultiSwipe(
        lines: List<Pair<PointF, PointF>>,
        onComplete: () -> Unit
    ) {
        Log.i("MultiTouch", "Executing 4-Finger Simultaneous Multi-Line Swipe...")
        accessibilityService.performMultiFingerSwipeLines(lines, durationMs = 380L) {
            handler.postDelayed({ onComplete() }, 150L)
        }
    }
}
