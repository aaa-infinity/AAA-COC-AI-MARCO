package com.cocai.autoclicker.engine

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

class UniversalScreenAdapter(context: Context) {
    var screenWidth: Int = 1920
        private set
    var screenHeight: Int = 1080
        private set
    var isLandscape: Boolean = true
        private set
    var aspectRatio: Float = 16f / 9f
        private set

    // Reference design resolution (Standard Full HD 1080p landscape)
    private val refWidth = 1920f
    private val refHeight = 1080f

    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        val w = metrics.widthPixels
        val h = metrics.heightPixels

        // Normalize to landscape since Clash of Clans runs horizontally
        screenWidth = max(w, h)
        screenHeight = min(w, h)
        isLandscape = true
        aspectRatio = screenWidth.toFloat() / screenHeight.toFloat()

        Log.i("UniversalAdapter", "Calibrated Screen: ${screenWidth}x${screenHeight} (Aspect Ratio: %.2f:1)".format(aspectRatio))
    }

    /**
     * Converts normalized percentage coordinates (0.0 to 1.0) to exact device pixels.
     */
    fun fromPercent(pctX: Float, pctY: Float): Pair<Float, Float> {
        val x = (pctX * screenWidth).coerceIn(5f, screenWidth - 5f)
        val y = (pctY * screenHeight).coerceIn(5f, screenHeight - 5f)
        return Pair(x, y)
    }

    /**
     * Scales reference coordinates (designed for 1920x1080) to any phone screen resolution.
     */
    fun scaleCoord(refX: Float, refY: Float): Pair<Float, Float> {
        val scaleX = screenWidth / refWidth
        val scaleY = screenHeight / refHeight
        val x = (refX * scaleX).coerceIn(5f, screenWidth - 5f)
        val y = (refY * scaleY).coerceIn(5f, screenHeight - 5f)
        return Pair(x, y)
    }
}
