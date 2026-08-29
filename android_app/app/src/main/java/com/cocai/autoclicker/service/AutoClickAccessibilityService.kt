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
import com.cocai.autoclicker.engine.ScreenCoordinateScaler
import com.cocai.autoclicker.engine.UiAnchor
import kotlin.random.Random

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }

    var emergencyStopListener: (() -> Unit)? = null
    lateinit var scaler: ScreenCoordinateScaler

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        scaler = ScreenCoordinateScaler(this)
        Log.i("AutoClickService", "Accessibility Service Connected with Dynamic Screen Scaling & Panic Stop!")
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
     * Single Tap with Dynamic Resolution Scaling and 80ms Contact Duration
     */
    fun performTap(
        x: Float,
        y: Float,
        durationMs: Long = 80L,
        anchor: UiAnchor = UiAnchor.CENTER_STAGE,
        jitter: Boolean = true,
        callback: (() -> Unit)? = null
    ) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        // Scale design coordinate to physical device resolution
        val scaled = scaler.scaleCoordinate(PointF(x, y), anchor)

        val jitterX = if (jitter) scaled.x + Random.nextDouble(-4.0, 4.0).toFloat() else scaled.x
        val jitterY = if (jitter) scaled.y + Random.nextDouble(-4.0, 4.0).toFloat() else scaled.y

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
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w("AutoClickService", "Gesture cancelled at ($jitterX, $jitterY)")
                callback?.invoke()
            }
        }, null)
    }

    /**
     * Multi-Touch Simultaneous Taps with Dynamic Scaling
     */
    fun performMultiTouchTaps(
        points: List<PointF>,
        durationMs: Long = 85L,
        anchor: UiAnchor = UiAnchor.DEPLOY_PERIMETER,
        callback: (() -> Unit)? = null
    ) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val builder = GestureDescription.Builder()
        for (pt in points.take(10)) {
            val scaled = scaler.scaleCoordinate(pt, anchor)
            val path = Path().apply {
                val jX = scaled.x + Random.nextDouble(-3.5, 3.5).toFloat()
                val jY = scaled.y + Random.nextDouble(-3.5, 3.5).toFloat()
                moveTo(jX, jY)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
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
        durationMs: Long = 320L,
        callback: (() -> Unit)? = null
    ) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val sP = scaler.scaleCoordinate(PointF(startX, startY), UiAnchor.DEPLOY_PERIMETER)
        val eP = scaler.scaleCoordinate(PointF(endX, endY), UiAnchor.DEPLOY_PERIMETER)

        val path = Path().apply {
            moveTo(sP.x, sP.y)
            val controlX = (sP.x + eP.x) / 2 + Random.nextInt(-25, 25)
            val controlY = (sP.y + eP.y) / 2 + Random.nextInt(-25, 25)
            quadTo(controlX.toFloat(), controlY.toFloat(), eP.x, eP.y)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
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
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val builder = GestureDescription.Builder()
        for (line in lines.take(5)) {
            val sP = scaler.scaleCoordinate(line.first, UiAnchor.DEPLOY_PERIMETER)
            val eP = scaler.scaleCoordinate(line.second, UiAnchor.DEPLOY_PERIMETER)

            val path = Path().apply {
                moveTo(sP.x, sP.y)
                val cX = (sP.x + eP.x) / 2 + Random.nextInt(-15, 15)
                val cY = (sP.y + eP.y) / 2 + Random.nextInt(-15, 15)
                quadTo(cX.toFloat(), cY.toFloat(), eP.x, eP.y)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                callback?.invoke()
            }
        }, null)
    }
}
