package com.cocai.autoclicker.ai

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * ⚡ Live Millisecond Latency Checker
 */
class ApiKeyPingEngine {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun pingProvider(
        providerUrl: String,
        apiKey: String,
        modelName: String,
        onSuccess: (latencyMs: Long, model: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        thread {
            val startTime = System.currentTimeMillis()
            var conn: HttpURLConnection? = null
            try {
                val pingUrl = if (providerUrl.contains("googleapis.com")) {
                    "$providerUrl/models?key=${apiKey.trim()}"
                } else {
                    "$providerUrl/models"
                }

                val url = URL(pingUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (!providerUrl.contains("googleapis.com")) {
                    conn.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }

                val code = conn.responseCode
                val latency = System.currentTimeMillis() - startTime
                if (code in 200..299) {
                    mainHandler.post { onSuccess(latency, modelName) }
                } else {
                    mainHandler.post { onError("HTTP $code (${latency}ms)") }
                }
            } catch (e: Exception) {
                mainHandler.post { onError(e.message ?: "Ping timeout") }
            } finally {
                conn?.disconnect()
            }
        }
    }
}
