package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * 👁️ ON-DEVICE VISION & TEMPLATE SCANNING ENGINE
 *
 * Combines high-speed HSV pixel sampling, game engine button detection,
 * and Google ML Kit OCR text recognition to read game state, loot numbers,
 * timers, and resource bars with zero cloud latency.
 */
class OnDeviceVisionEngine {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Scans the top-left loot counter area for Gold, Elixir, and Dark Elixir numbers
     */
    fun scanLootNumbers(
        bitmap: Bitmap,
        onResult: (gold: Long, elixir: Long, darkElixir: Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            // Crop top-left region where Clash of Clans displays loot during attacks
            val cropWidth = (bitmap.width * 0.30f).toInt().coerceAtMost(bitmap.width)
            val cropHeight = (bitmap.height * 0.28f).toInt().coerceAtMost(bitmap.height)
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, cropWidth, cropHeight)

            val image = InputImage.fromBitmap(croppedBitmap, 0)
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    var gold = 0L
                    var elixir = 0L
                    var darkElixir = 0L

                    val numbers = mutableListOf<Long>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val cleanText = line.text.replace(" ", "").replace(",", "").replace(".", "")
                            val num = cleanText.filter { it.isDigit() }.toLongOrNull()
                            if (num != null && num > 1000) {
                                numbers.add(num)
                            }
                        }
                    }

                    if (numbers.isNotEmpty()) gold = numbers.getOrNull(0) ?: 0L
                    if (numbers.size > 1) elixir = numbers.getOrNull(1) ?: 0L
                    if (numbers.size > 2) darkElixir = numbers.getOrNull(2) ?: 0L

                    Log.d("OnDeviceVision", "💰 OCR Loot: Gold=$gold | Elixir=$elixir | Dark=$darkElixir")
                    onResult(gold, elixir, darkElixir)
                }
                .addOnFailureListener { e ->
                    Log.w("OnDeviceVision", "OCR Scan error: ${e.message}")
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Samples center screen color to check if base has loaded (skips clouds)
     */
    fun isBaseLoaded(bitmap: Bitmap): Boolean {
        try {
            val centerX = bitmap.width / 2
            val centerY = bitmap.height / 2
            val pixel = bitmap.getPixel(centerX, centerY)

            val hsv = FloatArray(3)
            Color.colorToHSV(pixel, hsv)

            val hue = hsv[0]
            val sat = hsv[1]
            val value = hsv[2]

            // Green grass hue in Clash of Clans is typically between 75° and 145° with sat > 0.20
            val isGrass = (hue in 75f..145f) && (sat > 0.20f) && (value > 0.15f)
            return isGrass
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Checks if a Supercell "Connection Lost" / "Client Out of Sync" dialog is present
     */
    fun isConnectionLostDialog(bitmap: Bitmap): Boolean {
        try {
            val centerX = bitmap.width / 2
            val centerY = (bitmap.height * 0.60f).toInt()
            val pixel = bitmap.getPixel(centerX, centerY)

            val hsv = FloatArray(3)
            Color.colorToHSV(pixel, hsv)

            // Supercell reload button is orange/gold hue (Hue: 25°-45°, Sat: > 0.70)
            val isOrangeButton = (hsv[0] in 25f..45f) && (hsv[1] > 0.65f)
            return isOrangeButton
        } catch (e: Exception) {
            return false
        }
    }
}
