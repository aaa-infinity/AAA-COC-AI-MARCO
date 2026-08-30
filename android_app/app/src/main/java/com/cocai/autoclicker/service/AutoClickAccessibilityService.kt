package com.cocai.autoclicker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.cocai.autoclicker.engine.GaussianMotionCalibrator

/**
 * 👆 Master Touch & Multi-Touch Gesture Dispatcher
 */
class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
            private set
        val isServiceRunning: Boolean get() = instance != null
    }

    private val jitter = GaussianMotionCalibrator()
    var emergencyStopListener: (() -> Unit)? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i("AutoClickAcc", "✓ Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            Log.w("AutoClickAcc", "🚨 EMERGENCY STOP: Volume Down pressed!")
            emergencyStopListener?.invoke()
            return true
        }
        return super.onKeyEvent(event)
    }

    private fun getDimensions(): Pair<Int, Int> {
        val dm = resources.displayMetrics
        return Pair(dm.widthPixels, dm.heightPixels)
    }

    fun performPercentageTap(pct: PointF, durationMs: Long = 50L, onComplete: (() -> Unit)? = null) {
        val (w, h) = getDimensions()
        val jittered = jitter.applyJitter(pct)
        val x = jittered.x * w
        val y = jittered.y * h

        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke()
            }
        }, null)
    }

    fun performPercentageMultiTouch(points: List<PointF>, durationMs: Long = 60L, onComplete: (() -> Unit)? = null) {
        val (w, h) = getDimensions()
        val builder = GestureDescription.Builder()

        for (pt in points) {
            val j = jitter.applyJitter(pt)
            val path = Path().apply { moveTo(j.x * w, j.y * h) }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { onComplete?.invoke() }
            override fun onCancelled(gestureDescription: GestureDescription?) { onComplete?.invoke() }
        }, null)
    }

    fun performPercentageMultiFingerSwipes(lines: List<Pair<PointF, PointF>>, durationMs: Long = 400L, onComplete: (() -> Unit)? = null) {
        val (w, h) = getDimensions()
        val builder = GestureDescription.Builder()

        for ((start, end) in lines) {
            val s = jitter.applyJitter(start)
            val e = jitter.applyJitter(end)
            val path = Path().apply {
                moveTo(s.x * w, s.y * h)
                lineTo(e.x * w, e.y * h)
            }
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
        }

        dispatchGesture(builder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { onComplete?.invoke() }
            override fun onCancelled(gestureDescription: GestureDescription?) { onComplete?.invoke() }
        }, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
