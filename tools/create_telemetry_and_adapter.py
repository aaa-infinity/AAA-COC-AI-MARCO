import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

# 1. UniversalScreenAdapter.kt (Universal Device & Aspect Ratio Scaling)
screen_adapter = """package com.cocai.autoclicker.engine

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
"""

# 2. CrashTelemetryService.kt (On-Device Self-Healing & Diagnostic Telemetry)
telemetry_service = """package com.cocai.autoclicker.engine

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class CrashTelemetryService private constructor(private val context: Context) {

    private val logFile = File(context.filesDir, "telemetry_diagnostics.json")

    companion object {
        @Volatile
        private var instance: CrashTelemetryService? = null

        fun init(context: Context): CrashTelemetryService {
            return instance ?: synchronized(this) {
                instance ?: CrashTelemetryService(context.applicationContext).also {
                    instance = it
                    it.setupGlobalExceptionHandler()
                }
            }
        }

        fun get(): CrashTelemetryService? = instance
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        Log.i("Telemetry", "Global Crash & Diagnostic handler initialized.")
    }

    fun logCrash(throwable: Throwable) {
        try {
            val crashReport = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("device_model", Build.MODEL)
                put("manufacturer", Build.MANUFACTURER)
                put("android_version", Build.VERSION.RELEASE)
                put("sdk_int", Build.VERSION.SDK_INT)
                put("error_message", throwable.message ?: "Unknown error")
                put("stack_trace", Log.getStackTraceString(throwable))
            }

            val writer = FileWriter(logFile, true)
            writer.write(crashReport.toString() + "\n")
            writer.flush()
            writer.close()
            Log.e("Telemetry", "Crash report logged to ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("Telemetry", "Failed to log crash: ${e.message}")
        }
    }

    fun getDiagnosticSummary(): JSONObject {
        val dm = context.resources.displayMetrics
        return JSONObject().apply {
            put("app_version", "2.0-COMMERCIAL")
            put("device", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("android_os", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            put("screen_dpi", dm.densityDpi)
            put("screen_size", "${maxOf(dm.widthPixels, dm.heightPixels)}x${minOf(dm.widthPixels, dm.heightPixels)}")
            put("status", "HEALTHY")
        }
    }
}
"""

with open(f'{pkg_dir}/engine/UniversalScreenAdapter.kt', 'w') as f:
    f.write(screen_adapter)

with open(f'{pkg_dir}/engine/CrashTelemetryService.kt', 'w') as f:
    f.write(telemetry_service)

# 3. Update build-apk.yml with target Telegram channel @aaafreecloud
workflow_yml = """name: Build AAA COC AI MARCO APK

on:
  push:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-and-deliver:
    name: Build Commercial APK & Send to Telegram
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Grant Execute Permissions for Gradle
        working-directory: android_app
        run: chmod +x gradlew || true

      - name: Compile Commercial APK
        working-directory: android_app
        run: ./gradlew assembleDebug --stacktrace --no-daemon

      - name: Upload AAA COC AI MARCO APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: AAA-COC-AI-MARCO-APK
          path: android_app/app/build/outputs/apk/debug/*.apk
          retention-days: 30

      - name: Send APK to Telegram Channel @aaafreecloud
        if: success()
        env:
          TELEGRAM_BOT_TOKEN: ${{ secrets.TELEGRAM_BOT_TOKEN }}
          TELEGRAM_CHAT_ID: "@aaafreecloud"
        run: |
          if [ -n "$TELEGRAM_BOT_TOKEN" ]; then
            APK_FILE=$(find android_app/app/build/outputs/apk/debug -name "*.apk" | head -n 1)
            if [ -f "$APK_FILE" ]; then
              curl -F chat_id="${TELEGRAM_CHAT_ID}" \\
                   -F document=@"$APK_FILE" \\
                   -F caption="⚔️ AAA COC AI MARCO v2.0 Commercial APK Build Complete!%0A🐉 Dedicated Home Village Dragon Farming%0A📱 Supports all phone resolutions + Google ML Kit OCR" \\
                   https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendDocument
            fi
          else
            echo "TELEGRAM_BOT_TOKEN secret not configured in repository. Artifact uploaded to GitHub Artifacts."
          fi
"""

with open('.github/workflows/build-apk.yml', 'w') as f:
    f.write(workflow_yml)

print("Created UniversalScreenAdapter, CrashTelemetryService, and updated build-apk.yml")
