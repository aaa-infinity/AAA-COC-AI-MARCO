package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MlKitOcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Reads text and numbers (Gold, Elixir, Dark Elixir, Ores) on-device with ML Kit.
     */
    fun extractNumbersFromBitmap(bitmap: Bitmap, onComplete: (Long) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                val digitsOnly = rawText.replace("[^0-9]".toRegex(), "")
                val number = digitsOnly.toLongOrNull() ?: 0L
                onComplete(number)
            }
            .addOnFailureListener { e ->
                Log.e("MlKitOcr", "OCR extraction failed: ${e.message}")
                onComplete(0L)
            }
    }
}
