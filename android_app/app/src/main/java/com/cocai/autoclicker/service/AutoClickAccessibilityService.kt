package com.cocai.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.random.Random

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i("AutoClickService", "Accessibility Service Connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.w("AutoClickService", "Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun performTap(x: Float, y: Float, durationMs: Long = 50L, jitter: Boolean = true, callback: (() -> Unit)? = null) {
        val jitterX = if (jitter) x + Random.nextDouble(-4.0, 4.0).toFloat() else x
        val jitterY = if (jitter) y + Random.nextDouble(-4.0, 4.0).toFloat() else y

        val path = Path().apply {
            moveTo(jitterX, jitterY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }

    fun performBezierSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300L,
        callback: (() -> Unit)? = null
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            val controlX = (startX + endX) / 2 + Random.nextInt(-30, 30)
            val controlY = (startY + endY) / 2 + Random.nextInt(-30, 30)
            quadTo(controlX.toFloat(), controlY.toFloat(), endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }

    fun performMultiTouchTaps(points: List<Pair<Float, Float>>, durationMs: Long = 60L) {
        val builder = GestureDescription.Builder()
        for (pt in points.take(10)) {
            val path = Path().apply {
                val jX = pt.first + Random.nextDouble(-3.0, 3.0).toFloat()
                val jY = pt.second + Random.nextDouble(-3.0, 3.0).toFloat()
                moveTo(jX, jY)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }
        dispatchGesture(builder.build(), null, null)
    }
}
