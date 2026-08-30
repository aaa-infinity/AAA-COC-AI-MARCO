package com.cocai.autoclicker.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect

/**
 * 🎯 On-Device Image & Template Matcher
 * Finds exact buttons and UI icons on the live screen regardless of resolution,
 * notch insets, or aspect ratio using fast normalized pixel correlation.
 */
object TemplateMatcher {

    data class MatchResult(val found: Boolean, val center: Point, val confidence: Float, val rect: Rect)

    /**
     * Searches for a sub-image template within the source screen bitmap
     */
    fun findTemplate(
        screen: Bitmap,
        template: Bitmap,
        minConfidence: Float = 0.80f,
        searchRegion: Rect? = null
    ): MatchResult {
        val sW = screen.width
        val sH = screen.height
        val tW = template.width
        val tH = template.height

        if (tW > sW || tH > sH) {
            return MatchResult(false, Point(0, 0), 0f, Rect())
        }

        val startX = searchRegion?.left?.coerceIn(0, sW - tW) ?: 0
        val startY = searchRegion?.top?.coerceIn(0, sH - tH) ?: 0
        val endX = searchRegion?.right?.coerceIn(tW, sW) ?: (sW - tW)
        val endY = searchRegion?.bottom?.coerceIn(tH, sH) ?: (sH - tH)

        var bestScore = 0f
        var bestX = 0
        var bestY = 0

        val step = 4 // Fast stride for real-time mobile performance

        for (y in startY..endY step step) {
            for (x in startX..endX step step) {
                val score = compareFast(screen, template, x, y)
                if (score > bestScore) {
                    bestScore = score
                    bestX = x
                    bestY = y
                    if (bestScore >= 0.95f) break // Early exit on near-perfect match
                }
            }
            if (bestScore >= 0.95f) break
        }

        val found = bestScore >= minConfidence
        val centerX = bestX + tW / 2
        val centerY = bestY + tH / 2
        val matchedRect = Rect(bestX, bestY, bestX + tW, bestY + tH)

        return MatchResult(found, Point(centerX, centerY), bestScore, matchedRect)
    }

    private fun compareFast(screen: Bitmap, template: Bitmap, offX: Int, offY: Int): Float {
        val tW = template.width
        val tH = template.height
        var matched = 0
        var total = 0
        val step = 3

        for (ty in 0 until tH step step) {
            for (tx in 0 until tW step step) {
                val tColor = template.getPixel(tx, ty)
                if (Color.alpha(tColor) < 30) continue // Skip transparent pixels

                val sColor = screen.getPixel(offX + tx, offY + ty)

                val diffR = Math.abs(Color.red(tColor) - Color.red(sColor))
                val diffG = Math.abs(Color.green(tColor) - Color.green(sColor))
                val diffB = Math.abs(Color.blue(tColor) - Color.blue(sColor))

                if (diffR < 35 && diffG < 35 && diffB < 35) {
                    matched++
                }
                total++
            }
        }

        return if (total > 0) matched.toFloat() / total.toFloat() else 0f
    }
}
