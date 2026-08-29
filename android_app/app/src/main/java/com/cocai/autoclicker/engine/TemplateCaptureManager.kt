package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class TemplateCaptureManager(private val context: Context) {
    private val templateDir = File(context.filesDir, "game_templates").apply { mkdirs() }

    /**
     * Saves a cropped region of the screen as an exact, live in-game template.
     */
    fun saveCropAsTemplate(screenBitmap: Bitmap, x: Int, y: Int, width: Int, height: Int, templateName: String): Boolean {
        return try {
            val clampedX = x.coerceIn(0, screenBitmap.width - 1)
            val clampedY = y.coerceIn(0, screenBitmap.height - 1)
            val clampedW = width.coerceAtMost(screenBitmap.width - clampedX)
            val clampedH = height.coerceAtMost(screenBitmap.height - clampedY)

            if (clampedW <= 0 || clampedH <= 0) return false

            val cropped = Bitmap.createBitmap(screenBitmap, clampedX, clampedY, clampedW, clampedH)
            val destFile = File(templateDir, "$templateName.png")
            val out = FileOutputStream(destFile)
            cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Log.i("TemplateCapture", "Saved live game asset template: ${destFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e("TemplateCapture", "Failed to save template $templateName: ${e.message}")
            false
        }
    }

    /**
     * Loads a previously saved template bitmap from local storage.
     */
    fun loadTemplate(templateName: String): Bitmap? {
        val file = File(templateDir, "$templateName.png")
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun listSavedTemplates(): List<String> {
        return templateDir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }
}
