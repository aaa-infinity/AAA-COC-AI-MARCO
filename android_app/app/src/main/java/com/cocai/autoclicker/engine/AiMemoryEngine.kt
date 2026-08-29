package com.cocai.autoclicker.engine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class AttackRecord(
    val timestamp: Long,
    val entryAngle: String,      // "BOTTOM_LEFT", "TOP_LEFT", "BOTTOM_RIGHT", "TOP_RIGHT"
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
    private val allSides = listOf("BOTTOM_LEFT", "TOP_LEFT", "BOTTOM_RIGHT", "TOP_RIGHT")

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
        Log.i("AiMemory", "Recorded battle memory. Total learned samples: ${attackHistory.size}")
    }

    /**
     * Epsilon-Greedy Reinforcement Learning Bandit:
     * - 90% Exploitation of best historical attack angle
     * - 10% Exploration of alternative attack angles
     */
    fun getOptimalEntrySide(epsilon: Double = 0.10): String {
        if (attackHistory.size < 3 || Random.nextDouble() < epsilon) {
            val randomSide = allSides.random()
            Log.d("AiMemory", "Bandit Exploration: chosen $randomSide")
            return randomSide
        }

        val sideScores = mutableMapOf<String, Double>()
        val sideCounts = mutableMapOf<String, Int>()

        for (r in attackHistory.takeLast(40)) {
            // Reward function = (Stars * 35) + (Destruction * 0.65) + (Loot / 100,000)
            val lootReward = ((r.goldLooted + r.elixirLooted) / 100000.0).coerceAtMost(20.0)
            val score = (r.stars * 35.0) + (r.destructionPercent * 0.65) + lootReward
            sideScores[r.entryAngle] = sideScores.getOrDefault(r.entryAngle, 0.0) + score
            sideCounts[r.entryAngle] = sideCounts.getOrDefault(r.entryAngle, 0) + 1
        }

        var bestSide = "BOTTOM_LEFT"
        var highestAvgScore = -1.0

        for (side in allSides) {
            val count = sideCounts[side] ?: 0
            if (count > 0) {
                val avg = (sideScores[side] ?: 0.0) / count
                if (avg > highestAvgScore) {
                    highestAvgScore = avg
                    bestSide = side
                }
            }
        }

        Log.i("AiMemory", "Bandit Exploitation: selected best learned side: $bestSide (Avg Score: $highestAvgScore)")
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
        for (r in attackHistory.takeLast(100)) {
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
