import os

pkg_dir = 'android_app/app/src/main/java/com/cocai/autoclicker'

# 1. ApiKeyRotator.kt - Multi-Key Manager with Auto-Rotation on Rate Limits (429)
key_rotator = """package com.cocai.autoclicker.engine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ApiKeyStatus(
    val key: String,
    var isRateLimited: Boolean = false,
    var rateLimitResetTime: Long = 0L,
    var totalRequests: Int = 0,
    var failedRequests: Int = 0
)

class ApiKeyRotator(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_provider_prefs", Context.MODE_PRIVATE)
    private val keyPool = mutableListOf<ApiKeyStatus>()
    private var currentIndex = 0

    init {
        loadKeysFromPrefs()
    }

    @Synchronized
    fun addKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isNotEmpty() && keyPool.none { it.key == trimmed }) {
            keyPool.add(ApiKeyStatus(trimmed))
            saveKeysToPrefs()
            Log.i("ApiKeyRotator", "Added new API key. Total keys in pool: ${keyPool.size}")
        }
    }

    @Synchronized
    fun removeKey(key: String) {
        keyPool.removeAll { it.key == key }
        saveKeysToPrefs()
    }

    @Synchronized
    fun getAllKeys(): List<ApiKeyStatus> = keyPool.toList()

    @Synchronized
    fun getActiveKey(): String? {
        if (keyPool.isEmpty()) return null
        val now = System.currentTimeMillis()

        // Unmark expired rate limits (after 60 seconds)
        for (k in keyPool) {
            if (k.isRateLimited && now > k.rateLimitResetTime) {
                k.isRateLimited = false
                Log.i("ApiKeyRotator", "Key ${k.key.take(6)}... cooldown expired. Returned to active pool.")
            }
        }

        // Find next non-rate-limited key
        for (i in 0 until keyPool.size) {
            val idx = (currentIndex + i) % keyPool.size
            val candidate = keyPool[idx]
            if (!candidate.isRateLimited) {
                currentIndex = idx
                candidate.totalRequests++
                return candidate.key
            }
        }

        Log.w("ApiKeyRotator", "All API keys are currently rate-limited (429)! Retrying primary key.")
        return keyPool.firstOrNull()?.key
    }

    @Synchronized
    fun reportRateLimit(key: String, cooldownSeconds: Long = 60L) {
        val entry = keyPool.find { it.key == key }
        if (entry != null) {
            entry.isRateLimited = true
            entry.failedRequests++
            entry.rateLimitResetTime = System.currentTimeMillis() + (cooldownSeconds * 1000L)
            Log.w("ApiKeyRotator", "API Key ${key.take(6)}... hit 429 Rate Limit. Rotating to next key!")
            // Advance index to rotate immediately
            currentIndex = (currentIndex + 1) % keyPool.size
        }
    }

    private fun saveKeysToPrefs() {
        val arr = JSONArray()
        for (k in keyPool) {
            arr.put(k.key)
        }
        prefs.edit().putString("key_pool", arr.toString()).apply()
    }

    private fun loadKeysFromPrefs() {
        keyPool.clear()
        val jsonStr = prefs.getString("key_pool", "[]") ?: "[]"
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val k = arr.getString(i)
                if (k.isNotBlank()) keyPool.add(ApiKeyStatus(k))
            }
        } catch (e: Exception) {
            Log.e("ApiKeyRotator", "Error loading keys: ${e.message}")
        }
    }
}
"""

# 2. LiveModelFetcher.kt - Dynamic Model Discovery from Provider
model_fetcher = """package com.cocai.autoclicker.engine

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
"""

# 3. AiMemoryEngine.kt - Self-Improving Attack Memory & Learning System
ai_memory = """package com.cocai.autoclicker.engine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class AttackRecord(
    val timestamp: Long,
    val entryAngle: String,      // "BOTTOM_LEFT", "TOP_RIGHT", etc.
    val zapSuccess: Boolean,
    val goldLooted: Long,
    val elixirLooted: Long,
    val darkLooted: Long,
    val stars: Int,
    val destructionPercent: Int
)

class AiMemoryEngine(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_memory_db", Context.MODE_PRIVATE)
    private val attackHistory = mutableListOf<AttackRecord>()

    init {
        loadMemory()
    }

    @Synchronized
    fun recordAttackResult(
        entryAngle: String,
        zapSuccess: Boolean,
        gold: Long,
        elixir: Long,
        dark: Long,
        stars: Int,
        destruction: Int
    ) {
        val record = AttackRecord(
            timestamp = System.currentTimeMillis(),
            entryAngle = entryAngle,
            zapSuccess = zapSuccess,
            goldLooted = gold,
            elixirLooted = elixir,
            darkLooted = dark,
            stars = stars,
            destructionPercent = destruction
        )
        attackHistory.add(record)
        saveMemory()
        Log.i("AiMemory", "Recorded attack. Total memory samples: ${attackHistory.size}")
    }

    /**
     * Self-Improving Heuristic:
     * Analyzes past battle outcomes to recommend the highest-yielding entry side.
     */
    fun getOptimalEntrySide(): String {
        if (attackHistory.size < 3) return "BOTTOM_LEFT" // Default baseline

        val sideScores = mutableMapOf<String, Double>()
        val sideCounts = mutableMapOf<String, Int>()

        for (r in attackHistory.takeLast(30)) {
            val score = r.stars * 35.0 + r.destructionPercent * 0.65
            sideScores[r.entryAngle] = sideScores.getOrDefault(r.entryAngle, 0.0) + score
            sideCounts[r.entryAngle] = sideCounts.getOrDefault(r.entryAngle, 0) + 1
        }

        var bestSide = "BOTTOM_LEFT"
        var highestAvgScore = -1.0

        for ((side, totalScore) in sideScores) {
            val count = sideCounts[side] ?: 1
            val avg = totalScore / count
            if (avg > highestAvgScore) {
                highestAvgScore = avg
                bestSide = side
            }
        }

        Log.i("AiMemory", "AI Self-Improvement selected best entry side: $bestSide (Avg Score: $highestAvgScore)")
        return bestSide
    }

    fun getSuccessStatistics(): JSONObject {
        val total = attackHistory.size
        if (total == 0) return JSONObject().put("total_raids", 0).put("win_rate", "0%")

        val threeStars = attackHistory.count { it.stars >= 3 }
        val twoStars = attackHistory.count { it.stars == 2 }
        val totalGold = attackHistory.sumOf { it.goldLooted }
        val totalElixir = attackHistory.sumOf { it.elixirLooted }
        val winRate = ((threeStars + twoStars).toDouble() / total.toDouble()) * 100.0

        return JSONObject()
            .put("total_raids", total)
            .put("win_rate", "%.1f%%".format(winRate))
            .put("three_star_rate", "%.1f%%".format((threeStars.toDouble() / total) * 100.0))
            .put("total_gold_harvested", totalGold)
            .put("total_elixir_harvested", totalElixir)
    }

    private fun saveMemory() {
        val arr = JSONArray()
        for (r in attackHistory.takeLast(100)) { // Keep last 100 battles
            val obj = JSONObject()
                .put("ts", r.timestamp)
                .put("angle", r.entryAngle)
                .put("zap", r.zapSuccess)
                .put("gold", r.goldLooted)
                .put("elixir", r.elixirLooted)
                .put("dark", r.darkLooted)
                .put("stars", r.stars)
                .put("dest", r.destructionPercent)
            arr.put(obj)
        }
        prefs.edit().putString("attack_history", arr.toString()).apply()
    }

    private fun loadMemory() {
        attackHistory.clear()
        val jsonStr = prefs.getString("attack_history", "[]") ?: "[]"
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                attackHistory.add(
                    AttackRecord(
                        timestamp = o.getLong("ts"),
                        entryAngle = o.getString("angle"),
                        zapSuccess = o.getBoolean("zap"),
                        goldLooted = o.getLong("gold"),
                        elixirLooted = o.getLong("elixir"),
                        darkLooted = o.getLong("dark"),
                        stars = o.getInt("stars"),
                        destructionPercent = o.getInt("dest")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AiMemory", "Error loading attack history: ${e.message}")
        }
    }
}
"""

with open(f'{pkg_dir}/engine/ApiKeyRotator.kt', 'w') as f:
    f.write(key_rotator)

with open(f'{pkg_dir}/engine/LiveModelFetcher.kt', 'w') as f:
    f.write(model_fetcher)

with open(f'{pkg_dir}/engine/AiMemoryEngine.kt', 'w') as f:
    f.write(ai_memory)

print("Created ApiKeyRotator, LiveModelFetcher, and AiMemoryEngine Kotlin classes.")
