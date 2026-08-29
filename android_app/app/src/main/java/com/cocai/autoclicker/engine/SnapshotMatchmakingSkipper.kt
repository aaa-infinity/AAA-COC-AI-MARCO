package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

/**
 * ⚡ Snapshot Matchmaking Fast-Skipper
 *
 * Detects Supercell "Snapshot Bases" (instant cached copies of shielded bases)
 * using rapid on-device HSV sampling, allowing sub-500ms fast skipping without cloud delays.
 */
class SnapshotMatchmakingSkipper {

    fun isSnapshotBaseReady(bitmap: Bitmap): Boolean {
        // Sample 4 center-diagonal pixels for grass green HSV signature (Hue: 60-140)
        val w = bitmap.width
        val h = bitmap.height

        val samplePoints = listOf(
            Pair((w * 0.30f).toInt(), (h * 0.30f).toInt()),
            Pair((w * 0.70f).toInt(), (h * 0.30f).toInt()),
            Pair((w * 0.30f).toInt(), (h * 0.70f).toInt()),
            Pair((w * 0.70f).toInt(), (h * 0.70f).toInt())
        )

        var grassCount = 0
        val hsv = FloatArray(3)

        for (pt in samplePoints) {
            val pixel = bitmap.getPixel(pt.first, pt.second)
            Color.colorToHSV(pixel, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val valBrightness = hsv[2]

            // Village grass is typically Hue 65° to 135°, Sat > 0.25, Brightness > 0.20
            if (hue in 65f..135f && sat > 0.25f && valBrightness > 0.20f) {
                grassCount++
            }
        }

        val isReady = grassCount >= 3
        Log.d("SnapshotSkipper", "Snapshot Detection: $grassCount/4 grass points ($isReady)")
        return isReady
    }
}
