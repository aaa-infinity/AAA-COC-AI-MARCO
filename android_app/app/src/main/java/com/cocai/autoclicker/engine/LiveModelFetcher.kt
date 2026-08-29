package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class LiveModelFetcher {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun fetchLiveModels(
        providerUrl: String,
        apiKey: String,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            try {
                // Determine models endpoint
                val endpoint = when {
                    providerUrl.contains("generativelanguage.googleapis.com") ->
                        "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                    providerUrl.contains("api.groq.com") ->
                        "https://api.groq.com/openai/v1/models"
                    providerUrl.contains("openrouter.ai") ->
                        "https://openrouter.ai/api/v1/models"
                    else ->
                        if (providerUrl.endsWith("/v1")) "$providerUrl/models" else "$providerUrl/v1/models"
                }

                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                if (!providerUrl.contains("generativelanguage.googleapis.com")) {
                    conn.setRequestProperty("Authorization", "Bearer $apiKey")
                }
                conn.setRequestProperty("Accept", "application/json")

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val modelList = parseModelsJson(response)
                    mainHandler.post { onSuccess(modelList) }
                } else {
                    mainHandler.post { onError("HTTP $responseCode: ${conn.responseMessage}") }
                }
            } catch (e: Exception) {
                Log.e("LiveModelFetcher", "Failed to fetch models: ${e.message}")
                mainHandler.post { onError(e.localizedMessage ?: "Connection error") }
            }
        }
    }

    private fun parseModelsJson(jsonString: String): List<String> {
        val models = mutableListOf<String>()
        try {
            val json = JSONObject(jsonString)
            if (json.has("models")) {
                val arr = json.getJSONArray("models")
                for (i in 0 until arr.length()) {
                    val m = arr.getJSONObject(i)
                    val name = m.optString("name", "").replace("models/", "")
                    if (name.isNotBlank()) models.add(name)
                }
            } else if (json.has("data")) {
                val arr = json.getJSONArray("data")
                for (i in 0 until arr.length()) {
                    val m = arr.getJSONObject(i)
                    val id = m.optString("id", "")
                    if (id.isNotBlank()) models.add(id)
                }
            }
        } catch (e: Exception) {
            Log.e("LiveModelFetcher", "Error parsing model JSON: ${e.message}")
        }
        return if (models.isNotEmpty()) models else listOf("gemini-2.0-flash", "gemini-1.5-flash", "llama-3.3-70b-versatile", "gpt-4o-mini")
    }
}
