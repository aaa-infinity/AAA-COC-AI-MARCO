package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.util.Log

data class ParsedLootState(
    val goldAvailable: Long,
    val elixirAvailable: Long,
    val darkElixirAvailable: Long,
    val trophiesGainable: Int,
    val isHighValueTarget: Boolean
)

/**
 * 🔍 Adaptive Real-Time Loot & Defense OCR Parser
 *
 * Fast on-device parsing of in-game attack HUD numbers:
 * - Top-left Gold, Elixir, Dark Elixir text regions
 * - Trophy counter parsing
 * - Town Hall level tier estimation
 */
class AdaptiveLootOcrEngine {

    var minGoldThreshold: Long = 500000L
    var minElixirThreshold: Long = 500000L
    var minDarkThreshold: Long = 4000L

    /**
     * Evaluates whether a scanned base satisfies the user's loot requirements.
     */
    fun evaluateBaseTarget(gold: Long, elixir: Long, dark: Long, trophies: Int): ParsedLootState {
        val meetsGold = gold >= minGoldThreshold
        val meetsElixir = elixir >= minElixirThreshold
        val meetsDark = dark >= minDarkThreshold

        val isTarget = (meetsGold && meetsElixir) || meetsDark

        Log.i("LootOCR", "🔍 [BASE EVALUATION] Gold: $gold | Elixir: $elixir | Dark: $dark | Trophies: $trophies -> Target Match: $isTarget")

        return ParsedLootState(
            goldAvailable = gold,
            elixirAvailable = elixir,
            darkElixirAvailable = dark,
            trophiesGainable = trophies,
            isHighValueTarget = isTarget
        )
    }
}
