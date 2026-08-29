import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

# 1. Update app/build.gradle.kts with ML Kit Vision & AndroidX libraries
build_gradle = """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cocai.autoclicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cocai.autoclicker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
}
"""

with open('android_app/app/build.gradle.kts', 'w') as f:
    f.write(build_gradle)

# 2. TemplateCaptureManager.kt (Live In-Game Template Snapper like Macrorify)
template_capture_manager = """package com.cocai.autoclicker.engine

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
"""

with open(f'{pkg_dir}/engine/TemplateCaptureManager.kt', 'w') as f:
    f.write(template_capture_manager)

# 3. MlKitOcrEngine.kt (On-Device OCR using Google ML Kit)
mlkit_ocr = """package com.cocai.autoclicker.engine

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
"""

with open(f'{pkg_dir}/engine/MlKitOcrEngine.kt', 'w') as f:
    f.write(mlkit_ocr)

print("Configured native Android ML Kit OCR and Live Template Capture Manager.")
