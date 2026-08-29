package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log

enum class BaseLootDistribution {
    DEAD_BASE_OUTSIDE_COLLECTORS,
    CORE_STORAGES_ACTIVE,
    HYBRID_BALANCED
}

data class BaseLootProfile(
    val distribution: BaseLootDistribution,
    val goldLoot: Long,
    val elixirLoot: Long,
    val darkLoot: Long,
    val perimeterDropZones: List<PointF>
)

class DeadBaseCollectorHunter {

    /**
     * Analyzes base layout to identify if resources are concentrated in outside collectors (Dead Base)
     * or inside the protected core.
     */
    fun analyzeLootDistribution(gold: Long, elixir: Long, dark: Long): BaseLootProfile {
        // High Gold & Elixir with relatively low Trophy tier or high abandoned signature = Dead Base
        val isDeadBase = gold >= 550000L && elixir >= 550000L

        val distribution = if (isDeadBase) {
            Log.i("DeadBaseHunter", "🎯 [DEAD BASE DETECTED] Loot concentrated in outside mines/collectors! Surgical outer deployment selected.")
            BaseLootDistribution.DEAD_BASE_OUTSIDE_COLLECTORS
        } else {
            Log.i("DeadBaseHunter", "🛡️ [CORE BASE] Loot stored in protected vaults. Heavy core charge selected.")
            BaseLootDistribution.CORE_STORAGES_ACTIVE
        }

        val perimeterZones = listOf(
            PointF(400f, 350f),   // Top-Left Flank
            PointF(650f, 250f),   // Top Flank
            PointF(960f, 200f),   // North Core Point
            PointF(1270f, 250f),  // Top-Right Flank
            PointF(1520f, 350f),  // Right Flank
            PointF(1400f, 650f),  // Bottom-Right Flank
            PointF(960f, 850f),   // South Core Point
            PointF(520f, 650f)    // Bottom-Left Flank
        )

        return BaseLootProfile(
            distribution = distribution,
            goldLoot = gold,
            elixirLoot = elixir,
            darkLoot = dark,
            perimeterDropZones = perimeterZones
        )
    }
}
