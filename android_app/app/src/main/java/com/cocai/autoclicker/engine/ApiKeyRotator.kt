package com.cocai.autoclicker.engine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray

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
    fun clearAllKeys() {
        keyPool.clear()
        saveKeysToPrefs()
        Log.i("ApiKeyRotator", "Cleared all API keys from pool.")
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
            if (keyPool.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % keyPool.size
            }
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
                keyPool.add(ApiKeyStatus(arr.getString(i)))
            }
        } catch (e: Exception) {
            Log.e("ApiKeyRotator", "Failed to load keys from prefs: ${e.message}")
        }
    }
}
