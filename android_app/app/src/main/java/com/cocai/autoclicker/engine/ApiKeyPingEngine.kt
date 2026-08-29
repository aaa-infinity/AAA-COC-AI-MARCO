package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * ⚡ Live API Key Ping & Validation Engine
 *
 * Dispatches lightweight health-check pings to Gemini, OpenRouter, Groq, or DeepSeek
 * measuring exact round-trip latency in milliseconds.
 */
class ApiKeyPingEngine {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun pingProvider(
        providerUrl: String,
        apiKey: String,
        modelName: String,
        onSuccess: (latencyMs: Long, message: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        thread {
            val startTime = System.currentTimeMillis()
            var conn: HttpURLConnection? = null
            try {
                val isGemini = providerUrl.contains("googleapis", ignoreCase = true)
                val targetUrl = if (isGemini) {
                    "$providerUrl/models/$modelName:generateContent?key=$apiKey"
                } else {
                    "$providerUrl/chat/completions"
                }

                val url = URL(targetUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                if (!isGemini) {
                    conn.setRequestProperty("Authorization", "Bearer $apiKey")
                }

                conn.doOutput = true

                val payload = if (isGemini) {
                    JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", "ping"))
                                })
                            })
                        })
                    }
                } else {
                    JSONObject().apply {
                        put("model", modelName)
                        put("max_tokens", 5)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "ping")
                            })
                        })
                    }
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                val latencyMs = System.currentTimeMillis() - startTime

                if (responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseStr = reader.readText()
                    reader.close()
                    Log.i("ApiKeyPing", "✓ Ping Success (${latencyMs}ms) to $modelName")
                    mainHandler.post {
                        onSuccess(latencyMs, "⚡ Ping: ${latencyMs}ms | $modelName Online & Validated!")
                    }
                } else {
                    val errStream = conn.errorStream
                    val errBody = if (errStream != null) BufferedReader(InputStreamReader(errStream)).readText() else "HTTP $responseCode"
                    Log.w("ApiKeyPing", "❌ Ping Failed (HTTP $responseCode): $errBody")
                    mainHandler.post {
                        onError("❌ HTTP $responseCode: ${if (responseCode == 401) "Invalid API Key" else if (responseCode == 429) "Rate Limit/Quota Exceeded" else "Provider Offline"}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ApiKeyPing", "Ping exception: ${e.message}")
                mainHandler.post {
                    onError("❌ Connection Error: ${e.localizedMessage ?: "Timeout"}")
                }
            } finally {
                conn?.disconnect()
            }
        }
    }
}
