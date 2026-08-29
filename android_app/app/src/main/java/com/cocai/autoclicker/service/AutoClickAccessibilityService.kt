package com.cocai.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlin.random.Random

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }

    var emergencyStopListener: (() -> Unit)? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i("AutoClickService", "Accessibility Service Connected with Multi-Touch & Hardware Panic Stop!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.w("AutoClickService", "Accessibility Service Interrupted")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        // Hardware Volume Down Key Override for Emergency Panic Stop
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            Log.w("AutoClickService", "🚨 [PANIC STOP] Hardware Volume Down pressed! Halting all macro actions.")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, "🚨 EMERGENCY PANIC STOP: Macro Halted via Volume Down!", Toast.LENGTH_LONG).show()
                emergencyStopListener?.invoke()
            }
            return true // Consume key event
        }
        return super.onKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Single Tap with Gaussian coordinate jitter
     */
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

    /**
     * Curved Bezier swipe gesture
     */
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
            val controlX = (startX + endX) / 2 + Random.nextInt(-25, 25)
            val controlY = (startY + endY) / 2 + Random.nextInt(-25, 25)
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

    /**
     * Multi-Touch Simultaneous Taps (2, 3, 4, or up to 10 simultaneous finger drops)
     */
    fun performMultiTouchTaps(points: List<PointF>, durationMs: Long = 65L, callback: (() -> Unit)? = null) {
        val builder = GestureDescription.Builder()
        for (pt in points.take(10)) {
            val path = Path().apply {
                val jX = pt.x + Random.nextDouble(-3.5, 3.5).toFloat()
                val jY = pt.y + Random.nextDouble(-3.5, 3.5).toFloat()
                moveTo(jX, jY)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }

    /**
     * Multi-Finger Simultaneous Line Deployment (2, 3, or 4 fingers swiping simultaneously)
     */
    fun performMultiFingerSwipeLines(
        lines: List<Pair<PointF, PointF>>,
        durationMs: Long = 350L,
        callback: (() -> Unit)? = null
    ) {
        val builder = GestureDescription.Builder()
        for (line in lines.take(5)) {
            val path = Path().apply {
                moveTo(line.first.x, line.first.y)
                val cX = (line.first.x + line.second.x) / 2 + Random.nextInt(-15, 15)
                val cY = (line.first.y + line.second.y) / 2 + Random.nextInt(-15, 15)
                quadTo(cX.toFloat(), cY.toFloat(), line.second.x, line.second.y)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }

    /**
     * Smooth Two-Finger Pinch Zoom In / Out
     */
    fun performPinchZoom(centerX: Float, centerY: Float, zoomIn: Boolean, durationMs: Long = 400L, callback: (() -> Unit)? = null) {
        val spanStart = if (zoomIn) 80f else 300f
        val spanEnd = if (zoomIn) 300f else 80f

        val pathFinger1 = Path().apply {
            moveTo(centerX - spanStart, centerY - spanStart)
            lineTo(centerX - spanEnd, centerY - spanEnd)
        }

        val pathFinger2 = Path().apply {
            moveTo(centerX + spanStart, centerY + spanStart)
            lineTo(centerX + spanEnd, centerY + spanEnd)
        }

        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(pathFinger1, 0, durationMs))
        builder.addStroke(GestureDescription.StrokeDescription(pathFinger2, 0, durationMs))

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }
}
