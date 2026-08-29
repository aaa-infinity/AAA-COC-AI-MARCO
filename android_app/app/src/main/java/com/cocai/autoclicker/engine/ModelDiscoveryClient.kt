package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * 🌐 100% Dynamic Live Model Discovery Client
 *
 * Hits the provider's `/models` endpoint and returns ONLY real live models
 * active on the user's specific API key account (zero hardcoded static lists).
 */
class ModelDiscoveryClient {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun fetchLiveModels(
        provider: AiProviderEnum,
        apiKey: String,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (apiKey.isBlank()) {
            onError("Please enter your API key first")
            return
        }

        thread {
            var conn: HttpURLConnection? = null
            try {
                val urlStr = when (provider) {
                    AiProviderEnum.GOOGLE_AI_STUDIO -> "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                    AiProviderEnum.OPENROUTER -> "https://openrouter.ai/api/v1/models"
                    AiProviderEnum.GROQ -> "https://api.groq.com/openai/v1/models"
                    AiProviderEnum.CUSTOM_OPENAI -> "https://api.deepseek.com/v1/models"
                }

                val url = URL(urlStr)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                if (provider != AiProviderEnum.GOOGLE_AI_STUDIO) {
                    conn.setRequestProperty("Authorization", "Bearer $apiKey")
                }
                conn.setRequestProperty("Accept", "application/json")

                val code = conn.responseCode
                if (code in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val body = reader.readText()
                    reader.close()

                    val models = parseModelsList(body, provider)
                    if (models.isNotEmpty()) {
                        mainHandler.post { onSuccess(models) }
                    } else {
                        mainHandler.post { onError("No active models returned for this key") }
                    }
                } else {
                    val errStream = conn.errorStream
                    val errText = if (errStream != null) {
                        BufferedReader(InputStreamReader(errStream)).readText()
                    } else "HTTP $code"
                    mainHandler.post { onError("HTTP $code: $errText") }
                }
            } catch (e: Exception) {
                Log.e("ModelDiscovery", "Error fetching live models: ${e.message}")
                mainHandler.post { onError("Connection Error: ${e.localizedMessage ?: "Failed to connect"}") }
            } finally {
                conn?.disconnect()
            }
        }
    }

    private fun parseModelsList(rawJson: String, provider: AiProviderEnum): List<String> {
        val result = mutableListOf<String>()
        try {
            val root = JSONObject(rawJson)
            val dataArr = root.optJSONArray("models") ?: root.optJSONArray("data")
            if (dataArr != null) {
                for (i in 0 until dataArr.length()) {
                    val m = dataArr.getJSONObject(i)
                    var id = m.optString("id")
                    if (id.isEmpty()) {
                        id = m.optString("name").replace("models/", "")
                    }
                    if (id.isNotEmpty()) {
                        // Filter for relevant vision/language generation models
                        if (provider == AiProviderEnum.GOOGLE_AI_STUDIO) {
                            if (id.contains("gemini", ignoreCase = true) || id.contains("gemma", ignoreCase = true)) {
                                result.add(id)
                            }
                        } else {
                            result.add(id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("ModelDiscovery", "JSON parse error: ${e.message}")
        }
        return result.distinct()
    }
}
