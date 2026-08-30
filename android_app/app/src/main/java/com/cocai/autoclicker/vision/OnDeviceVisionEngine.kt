package com.cocai.autoclicker.vision

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * 👁️ On-Device Vision Engine
 * Uses Google ML Kit OCR and high-speed HSV pixel sampling to read
 * base loot numbers, skip clouds, and detect in-game popups in <50ms.
 */
class OnDeviceVisionEngine {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Scans top-left loot numbers (Gold, Elixir, Dark Elixir)
     */
    fun scanLoot(
        bitmap: Bitmap,
        onResult: (gold: Long, elixir: Long, darkElixir: Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val cropW = (bitmap.width * 0.32f).toInt().coerceAtMost(bitmap.width)
            val cropH = (bitmap.height * 0.30f).toInt().coerceAtMost(bitmap.height)
            val cropped = Bitmap.createBitmap(bitmap, 0, 0, cropW, cropH)

            val image = InputImage.fromBitmap(cropped, 0)
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val numbers = mutableListOf<Long>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val clean = line.text.replace(" ", "").replace(",", "").replace(".", "")
                            val parsed = clean.filter { it.isDigit() }.toLongOrNull()
                            if (parsed != null && parsed > 500) {
                                numbers.add(parsed)
                            }
                        }
                    }
                    val gold = numbers.getOrNull(0) ?: 0L
                    val elixir = numbers.getOrNull(1) ?: 0L
                    val darkElixir = numbers.getOrNull(2) ?: 0L
                    Log.d("VisionOCR", "💰 Loot Detected: Gold=$gold | Elixir=$elixir | Dark=$darkElixir")
                    onResult(gold, elixir, darkElixir)
                }
                .addOnFailureListener { e ->
                    Log.w("VisionOCR", "OCR Error: ${e.message}")
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Checks if cloud animation has passed and green village grass has loaded
     */
    fun isBaseLoaded(bitmap: Bitmap): Boolean {
        try {
            val cx = bitmap.width / 2
            val cy = bitmap.height / 2
            val pixel = bitmap.getPixel(cx, cy)
            val hsv = FloatArray(3)
            Color.colorToHSV(pixel, hsv)
            // Green grass hue: 75° - 145°
            return (hsv[0] in 75f..145f) && (hsv[1] > 0.20f) && (hsv[2] > 0.15f)
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Checks if Supercell connection lost reload dialog is visible
     */
    fun isReloadDialog(bitmap: Bitmap): Boolean {
        try {
            val cx = bitmap.width / 2
            val cy = (bitmap.height * 0.60f).toInt()
            val pixel = bitmap.getPixel(cx, cy)
            val hsv = FloatArray(3)
            Color.colorToHSV(pixel, hsv)
            // Orange reload button hue: 25° - 45°
            return (hsv[0] in 25f..45f) && (hsv[1] > 0.65f)
        } catch (e: Exception) {
            return false
        }
    }
}
