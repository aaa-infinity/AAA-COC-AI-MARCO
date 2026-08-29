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
        Log.i("AutoClickService", "Accessibility Service Connected with Universal Percentage Scaler!")
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
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Percentage Tap (0.0f - 1.0f Screen Percentage) with 85ms game-engine touch duration
     */
    fun performPercentageTap(
        pct: PointF,
        durationMs: Long = 85L,
        jitter: Boolean = true,
        callback: (() -> Unit)? = null
    ) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val px = scaler.toScreenPixel(pct)
        val jitterX = if (jitter) px.x + Random.nextDouble(-3.5, 3.5).toFloat() else px.x
        val jitterY = if (jitter) px.y + Random.nextDouble(-3.5, 3.5).toFloat() else px.y

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
                callback?.invoke()
            }
        }, null)
    }

    fun performPercentageTap(pctX: Float, pctY: Float, durationMs: Long = 85L, callback: (() -> Unit)? = null) {
        performPercentageTap(PointF(pctX, pctY), durationMs, jitter = true, callback)
    }

    /**
     * Pixel Tap (Backwards Compatible with Anchor Support)
     */
    fun performTap(
        x: Float,
        y: Float,
        durationMs: Long = 85L,
        anchor: UiAnchor = UiAnchor.CENTER_STAGE,
        jitter: Boolean = true,
        callback: (() -> Unit)? = null
    ) {
        val jitterX = if (jitter) x + Random.nextDouble(-3.5, 3.5).toFloat() else x
        val jitterY = if (jitter) y + Random.nextDouble(-3.5, 3.5).toFloat() else y

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
                callback?.invoke()
            }
        }, null)
    }

    /**
     * Multi-Touch Simultaneous Percentage Taps
     */
    fun performPercentageMultiTouch(
        pointsPct: List<PointF>,
        durationMs: Long = 85L,
        callback: (() -> Unit)? = null
    ) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val builder = GestureDescription.Builder()
        for (pct in pointsPct.take(10)) {
            val px = scaler.toScreenPixel(pct)
            val path = Path().apply {
                val jX = px.x + Random.nextDouble(-3.0, 3.0).toFloat()
                val jY = px.y + Random.nextDouble(-3.0, 3.0).toFloat()
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

    fun performMultiTouchTaps(
        points: List<PointF>,
        durationMs: Long = 85L,
        anchor: UiAnchor = UiAnchor.DEPLOY_PERIMETER,
        callback: (() -> Unit)? = null
    ) {
        val builder = GestureDescription.Builder()
        for (pt in points.take(10)) {
            val path = Path().apply {
                val jX = pt.x + Random.nextDouble(-3.0, 3.0).toFloat()
                val jY = pt.y + Random.nextDouble(-3.0, 3.0).toFloat()
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
     * Multi-Finger Simultaneous Percentage Line Swipes
     */
    fun performPercentageMultiFingerSwipes(
        linesPct: List<Pair<PointF, PointF>>,
        durationMs: Long = 380L,
        callback: (() -> Unit)? = null
    ) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val builder = GestureDescription.Builder()
        for (line in linesPct.take(5)) {
            val sP = scaler.toScreenPixel(line.first)
            val eP = scaler.toScreenPixel(line.second)

            val path = Path().apply {
                moveTo(sP.x, sP.y)
                val cX = (sP.x + eP.x) / 2 + Random.nextInt(-10, 10)
                val cY = (sP.y + eP.y) / 2 + Random.nextInt(-10, 10)
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

    /**
     * Multi-Finger Simultaneous Line Swipes (Pixel Backwards Compatible)
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
                val cX = (line.first.x + line.second.x) / 2 + Random.nextInt(-10, 10)
                val cY = (line.first.y + line.second.y) / 2 + Random.nextInt(-10, 10)
                quadTo(cX.toFloat(), cY.toFloat(), line.second.x, line.second.y)
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
     * Bezier Swipe (Backwards Compatible)
     */
    fun performBezierSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 350L,
        callback: (() -> Unit)? = null
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            val cX = (startX + endX) / 2 + Random.nextInt(-20, 20)
            val cY = (startY + endY) / 2 + Random.nextInt(-20, 20)
            quadTo(cX.toFloat(), cY.toFloat(), endX, endY)
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
     * Smooth Pinch Zoom Out
     */
    fun performPinchZoomOut(durationMs: Long = 450L, callback: (() -> Unit)? = null) {
        if (!::scaler.isInitialized) {
            scaler = ScreenCoordinateScaler(this)
        }

        val cX = scaler.screenWidth / 2f
        val cY = scaler.screenHeight / 2f

        val spanStart = 300f
        val spanEnd = 80f

        val pathFinger1 = Path().apply {
            moveTo(cX - spanStart, cY - spanStart)
            lineTo(cX - spanEnd, cY - spanEnd)
        }

        val pathFinger2 = Path().apply {
            moveTo(cX + spanStart, cY + spanStart)
            lineTo(cX + spanEnd, cY + spanEnd)
        }

        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(pathFinger1, 0, durationMs))
        builder.addStroke(GestureDescription.StrokeDescription(pathFinger2, 0, durationMs))

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
