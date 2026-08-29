package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

enum class UiAnchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER_STAGE,
    DEPLOY_PERIMETER
}

/**
 * 📐 Universal Screen Coordinate Scaler & Dynamic Aspect Ratio Normalizer
 *
 * Translates 1920x1080 design coordinates to any device resolution (2400x1080, 2412x1080, 1600x720, 2560x1440, etc.)
 * using edge-anchored geometry so buttons never miss regardless of phone model or aspect ratio!
 */
class ScreenCoordinateScaler(private val context: Context) {

    var screenWidth: Float = 1920f
        private set
    var screenHeight: Float = 1080f
        private set

    private val baseDesignWidth = 1920f
    private val baseDesignHeight = 1080f

    init {
        updateScreenDimensions()
    }

    fun updateScreenDimensions() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        // In landscape, width is always greater than height
        val rawW = max(metrics.widthPixels, metrics.heightPixels).toFloat()
        val rawH = min(metrics.widthPixels, metrics.heightPixels).toFloat()

        screenWidth = if (rawW > 0f) rawW else 1920f
        screenHeight = if (rawH > 0f) rawH else 1080f

        Log.i("CoordScaler", "📐 [SCREEN CALIBRATED] Physical Device: ${screenWidth.toInt()} x ${screenHeight.toInt()} (Aspect: ${String.format("%.2f", screenWidth / screenHeight)})")
    }

    /**
     * Scales a 1920x1080 design coordinate based on its UI Anchor region.
     */
    fun scaleCoordinate(designPt: PointF, anchor: UiAnchor = UiAnchor.CENTER_STAGE): PointF {
        val scaleY = screenHeight / baseDesignHeight

        return when (anchor) {
            UiAnchor.BOTTOM_LEFT -> {
                // Anchored to bottom-left corner (e.g. Attack button, Army camp overview)
                val x = designPt.x * scaleY
                val y = screenHeight - (baseDesignHeight - designPt.y) * scaleY
                PointF(x, y)
            }
            UiAnchor.BOTTOM_RIGHT -> {
                // Anchored to bottom-right corner (e.g. Find Match, Settings gear, Quick train)
                val x = screenWidth - (baseDesignWidth - designPt.x) * scaleY
                val y = screenHeight - (baseDesignHeight - designPt.y) * scaleY
                PointF(x, y)
            }
            UiAnchor.TOP_RIGHT -> {
                // Anchored to top-right corner (e.g. Close X button, Gem shop, Resources bar)
                val x = screenWidth - (baseDesignWidth - designPt.x) * scaleY
                val y = designPt.y * scaleY
                PointF(x, y)
            }
            UiAnchor.TOP_LEFT -> {
                // Anchored to top-left corner (e.g. Profile, Clan badge, Loot available in attack)
                val x = designPt.x * scaleY
                val y = designPt.y * scaleY
                PointF(x, y)
            }
            UiAnchor.CENTER_STAGE -> {
                // Anchored to center of screen (e.g. Town Hall, Wall dump, Village center)
                val offsetX = (designPt.x - 960f) * scaleY
                val offsetY = (designPt.y - 540f) * scaleY
                PointF(screenWidth / 2f + offsetX, screenHeight / 2f + offsetY)
            }
            UiAnchor.DEPLOY_PERIMETER -> {
                // Scales battlefield deployment bounds dynamically across wide screens
                val scaleX = screenWidth / baseDesignWidth
                PointF(designPt.x * scaleX, designPt.y * scaleY)
            }
        }
    }
}
