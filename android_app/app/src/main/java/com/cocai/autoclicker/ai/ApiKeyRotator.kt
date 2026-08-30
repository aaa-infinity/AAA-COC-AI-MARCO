package com.cocai.autoclicker.ai

import android.content.Context
import android.content.SharedPreferences

/**
 * 🔄 Multi-Key Pool & Auto-Rotator
 * Automatically fails over to the next key if a rate-limit (429/quota) occurs.
 */
class ApiKeyRotator(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("api_key_pool_prefs", Context.MODE_PRIVATE)
    private val keyList = mutableListOf<String>()
    private var currentIndex = 0

    init {
        loadKeys()
    }

    private fun loadKeys() {
        val raw = prefs.getString("keys_csv", "") ?: ""
        keyList.clear()
        if (raw.isNotEmpty()) {
            keyList.addAll(raw.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }

    fun addKey(key: String) {
        val clean = key.trim()
        if (clean.isNotEmpty() && !keyList.contains(clean)) {
            keyList.add(clean)
            saveKeys()
        }
    }

    fun getActiveKey(): String? {
        if (keyList.isEmpty()) return null
        return keyList[currentIndex % keyList.size]
    }

    fun rotateToNextKey(): String? {
        if (keyList.isEmpty()) return null
        currentIndex = (currentIndex + 1) % keyList.size
        return getActiveKey()
    }

    fun getAllKeys(): List<String> = keyList.toList()

    private fun saveKeys() {
        prefs.edit().putString("keys_csv", keyList.joinToString(",")).apply()
    }
}
