package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

data class MatchResult(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val confidence: Float
) {
    val centerX: Int get() = x + width / 2
    val centerY: Int get() = y + height / 2
}

class FastTemplateMatcher {

    fun findTemplate(
        screen: Bitmap,
        template: Bitmap,
        threshold: Float = 0.85f,
        step: Int = 4
    ): MatchResult? {
        val screenW = screen.width
        val screenH = screen.height
        val tmplW = template.width
        val tmplH = template.height

        if (tmplW > screenW || tmplH > screenH) return null

        var bestScore = 0.0f
        var bestX = 0
        var bestY = 0

        for (y in 0 until (screenH - tmplH) step step) {
            for (x in 0 until (screenW - tmplW) step step) {
                val score = compareRegion(screen, template, x, y, tmplW, tmplH)
                if (score > bestScore) {
                    bestScore = score
                    bestX = x
                    bestY = y
                    if (score >= 0.95f) {
                        return MatchResult(bestX, bestY, tmplW, tmplH, bestScore)
                    }
                }
            }
        }

        return if (bestScore >= threshold) {
            MatchResult(bestX, bestY, tmplW, tmplH, bestScore)
        } else {
            null
        }
    }

    private fun compareRegion(screen: Bitmap, tmpl: Bitmap, startX: Int, startY: Int, w: Int, h: Int): Float {
        var totalDiff = 0L
        val sampleStep = 3
        var sampledPixels = 0

        for (ty in 0 until h step sampleStep) {
            for (tx in 0 until w step sampleStep) {
                val screenPixel = screen.getPixel(startX + tx, startY + ty)
                val tmplPixel = tmpl.getPixel(tx, ty)

                val rDiff = abs(Color.red(screenPixel) - Color.red(tmplPixel))
                val gDiff = abs(Color.green(screenPixel) - Color.green(tmplPixel))
                val bDiff = abs(Color.blue(screenPixel) - Color.blue(tmplPixel))

                totalDiff += (rDiff + gDiff + bDiff)
                sampledPixels++
            }
        }

        val maxPossibleDiff = sampledPixels * 255L * 3L
        return 1.0f - (totalDiff.toFloat() / maxPossibleDiff.toFloat())
    }
}
