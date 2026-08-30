package com.cocai.autoclicker.ai

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
 * 🔑 100% Dynamic Model Discovery Client
 * Queries the live /models endpoint on the user's API key. Zero hardcoded lists.
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
            onError("API key cannot be empty")
            return
        }

        thread {
            var conn: HttpURLConnection? = null
            try {
                val endpointUrl = when (provider) {
                    AiProviderEnum.GOOGLE_AI_STUDIO -> "${provider.baseUrl}/models?key=${apiKey.trim()}"
                    else -> "${provider.baseUrl}/models"
                }

                val url = URL(endpointUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (provider != AiProviderEnum.GOOGLE_AI_STUDIO) {
                    conn.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val modelsList = mutableListOf<String>()
                    val json = JSONObject(response)

                    if (provider == AiProviderEnum.GOOGLE_AI_STUDIO && json.has("models")) {
                        val array = json.getJSONArray("models")
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val name = item.getString("name").replace("models/", "")
                            if (name.contains("gemini", true)) {
                                modelsList.add(name)
                            }
                        }
                    } else if (json.has("data")) {
                        val array = json.getJSONArray("data")
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val id = item.getString("id")
                            modelsList.add(id)
                        }
                    }

                    val result = if (modelsList.isNotEmpty()) modelsList.sorted() else listOf("default-vision-model")
                    mainHandler.post { onSuccess(result) }
                } else {
                    mainHandler.post { onError("HTTP $responseCode from ${provider.displayName}") }
                }
            } catch (e: Exception) {
                Log.e("ModelDiscovery", "Error fetching models: ${e.message}")
                mainHandler.post { onError(e.message ?: "Connection error") }
            } finally {
                conn?.disconnect()
            }
        }
    }
}
