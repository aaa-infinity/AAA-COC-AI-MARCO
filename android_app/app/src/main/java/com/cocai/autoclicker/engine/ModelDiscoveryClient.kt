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
 * 🌐 Dynamic Live Model Discovery Client
 *
 * Calls live endpoints to fetch available vision models:
 * - Google AI Studio: /models?key=...
 * - OpenRouter: /models
 * - Groq: /models
 */
class ModelDiscoveryClient {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun fetchLiveModels(
        provider: AiProviderEnum,
        apiKey: String,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        thread {
            var conn: HttpURLConnection? = null
            try {
                val urlStr = when (provider) {
                    AiProviderEnum.GOOGLE_AI_STUDIO -> "${provider.baseUrl}/models?key=$apiKey"
                    AiProviderEnum.OPENROUTER -> "${provider.baseUrl}/models"
                    AiProviderEnum.GROQ -> "${provider.baseUrl}/models"
                    AiProviderEnum.CUSTOM_OPENAI -> "${provider.baseUrl}/models"
                }

                val url = URL(urlStr)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (provider != AiProviderEnum.GOOGLE_AI_STUDIO && apiKey.isNotEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer $apiKey")
                }

                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val body = reader.readText()
                    reader.close()

                    val models = parseModelsList(body, provider)
                    mainHandler.post { onSuccess(models) }
                } else {
                    mainHandler.post { onError("HTTP ${conn.responseCode}") }
                }
            } catch (e: Exception) {
                Log.e("ModelDiscovery", "Error fetching models: ${e.message}")
                mainHandler.post { onError(e.localizedMessage ?: "Connection error") }
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
                    val name = m.optString("name").replace("models/", "")
                    val id = m.optString("id", name)
                    if (id.isNotEmpty()) result.add(id)
                }
            }
        } catch (e: Exception) {
            Log.w("ModelDiscovery", "JSON parsing error: ${e.message}")
        }
        return if (result.isNotEmpty()) result else listOf("gemini-2.0-flash", "llama-3.3-70b-versatile", "google/gemma-4-31b-it:free")
    }
}
