package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

/**
 * 📐 Percentage-Based Universal Screen Coordinator
 *
 * Converts 0.0f - 1.0f percentage coordinates directly into real physical screen pixels
 * across any phone resolution (1080p, 1440p, 720p, 2400x1080, 1600x720, etc.).
 */
class ScreenCoordinateScaler(private val context: Context) {

    var screenWidth: Float = 1920f
        private set
    var screenHeight: Float = 1080f
        private set

    init {
        updateScreenDimensions()
    }

    fun updateScreenDimensions() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        // In landscape orientation: width is the longer axis, height is the shorter axis
        val rawW = max(metrics.widthPixels, metrics.heightPixels).toFloat()
        val rawH = min(metrics.widthPixels, metrics.heightPixels).toFloat()

        screenWidth = if (rawW > 0f) rawW else 1920f
        screenHeight = if (rawH > 0f) rawH else 1080f

        Log.i("CoordScaler", "📐 [SCREEN CALIBRATED] Physical Device: ${screenWidth.toInt()} x ${screenHeight.toInt()} px")
    }

    fun toScreenPixel(pct: PointF): PointF {
        return PointF(pct.x * screenWidth, pct.y * screenHeight)
    }

    fun toScreenPixel(pctX: Float, pctY: Float): PointF {
        return PointF(pctX * screenWidth, pctY * screenHeight)
    }
}
