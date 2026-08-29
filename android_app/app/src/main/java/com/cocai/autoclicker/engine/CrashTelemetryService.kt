package com.cocai.autoclicker.engine

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.io.File

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

            logFile.appendText(crashReport.toString() + "\n")
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
