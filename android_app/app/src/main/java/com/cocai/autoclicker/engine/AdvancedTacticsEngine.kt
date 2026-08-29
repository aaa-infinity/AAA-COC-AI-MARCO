package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.math.atan2
import kotlin.random.Random

data class BaseEntity(
    val type: String, // "AIR_DEFENSE", "AIR_SWEEPER", "TOWN_HALL", "MONOLITH", "EAGLE"
    val x: Float,
    val y: Float,
    val facingAngleDeg: Float = 0f // For sweepers
)

data class TacticalPlan(
    val optimalEntrySide: String, // "BOTTOM_LEFT", "TOP_LEFT", "BOTTOM_RIGHT", "TOP_RIGHT"
    val startDeployLine: PointF,
    val endDeployLine: PointF,
    val zapTargets: List<PointF>,
    val rageSpellLocations: List<PointF>,
    val freezeSpellLocations: List<PointF>,
    val leftFunnelHero: PointF,
    val rightFunnelHero: PointF
)

class AdvancedTacticsEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    /**
     * Synthesizes an Advanced Tactical Attack Plan:
     * 1. Detects Air Sweeper orientation to attack from behind/flank.
     * 2. Computes the optimal Funnel points for King & Queen.
     * 3. Calculates Rage drop zones directly between core defenses.
     * 4. Prepares Multi-Touch 4-finger deployment vectors.
     */
    fun computeTacticalPlan(
        detectedEntities: List<BaseEntity> = emptyList(),
        preferredEntry: String = "AUTO"
    ): TacticalPlan {
        Log.i("TacticsEngine", "Computing tactical plan for ${detectedEntities.size} detected entities...")

        val airDefenses = detectedEntities.filter { it.type == "AIR_DEFENSE" }
        val sweepers = detectedEntities.filter { it.type == "AIR_SWEEPER" }
        val monoliths = detectedEntities.filter { it.type == "MONOLITH" }
        val townHall = detectedEntities.find { it.type == "TOWN_HALL" } ?: BaseEntity("TOWN_HALL", 960f, 540f)

        // Determine Entry Side based on Sweepers or User Preference
        val entrySide = if (preferredEntry != "AUTO") {
            preferredEntry
        } else if (sweepers.isNotEmpty()) {
            val primarySweeper = sweepers[0]
            if (primarySweeper.facingAngleDeg in 180f..360f) "BOTTOM_LEFT" else "TOP_RIGHT"
        } else {
            "BOTTOM_LEFT"
        }

        val (startLine, endLine, leftFunnel, rightFunnel) = when (entrySide) {
            "TOP_LEFT" -> Quad(
                PointF(400f, 400f), PointF(1000f, 200f),
                PointF(350f, 450f), PointF(1050f, 180f)
            )
            "TOP_RIGHT" -> Quad(
                PointF(1000f, 200f), PointF(1600f, 400f),
                PointF(950f, 180f), PointF(1650f, 450f)
            )
            "BOTTOM_RIGHT" -> Quad(
                PointF(1600f, 650f), PointF(1000f, 850f),
                PointF(1650f, 600f), PointF(950f, 880f)
            )
            else -> Quad( // BOTTOM_LEFT
                PointF(400f, 650f), PointF(1000f, 850f),
                PointF(350f, 600f), PointF(1050f, 880f)
            )
        }

        // Zap Targets: prioritize top 2 Air Defenses
        val zapTargets = if (airDefenses.size >= 2) {
            listOf(PointF(airDefenses[0].x, airDefenses[0].y), PointF(airDefenses[1].x, airDefenses[1].y))
        } else {
            listOf(PointF(750f, 480f), PointF(1170f, 480f))
        }

        // Rage in base core near Town Hall
        val rageLocations = listOf(
            PointF(townHall.x + Random.nextInt(-30, 30), townHall.y + Random.nextInt(-30, 30))
        )

        // Freeze on Monolith or Giga Inferno
        val freezeLocations = monoliths.map { PointF(it.x, it.y) }.ifEmpty {
            listOf(PointF(960f, 540f))
        }

        return TacticalPlan(
            optimalEntrySide = entrySide,
            startDeployLine = startLine,
            endDeployLine = endLine,
            zapTargets = zapTargets,
            rageSpellLocations = rageLocations,
            freezeSpellLocations = freezeLocations,
            leftFunnelHero = leftFunnel,
            rightFunnelHero = rightFunnel
        )
    }

    private data class Quad(val a: PointF, val b: PointF, val c: PointF, val d: PointF)
}
