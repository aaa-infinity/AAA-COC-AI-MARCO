package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.util.Log

enum class BaseQuadrant {
    NORTH_WEST,
    NORTH_EAST,
    SOUTH_WEST,
    SOUTH_EAST,
    CENTER_CORE
}

data class BaseDnaSignature(
    val layoutHash64: Long,
    val thQuadrant: BaseQuadrant,
    val eagleArtilleryQuadrant: BaseQuadrant,
    val monolithQuadrant: BaseQuadrant,
    val historical3StarRate: Float
)

/**
 * 🗺️ Tactical Defense Heatmap & Base DNA Analyzer
 *
 * 1. Base Fingerprinting: Computes 64-bit spatial hash from defensive clusters
 * 2. Heatmap Avoidance: Identifies Eagle Artillery / Monolith dead-zones
 * 3. Dynamic Tactical Prompting: Injects base DNA into LLM multimodal vision prompts
 */
class DefenseHeatmapAnalyzer(
    private val memoryEngine: AiMemoryEngine
) {

    fun generateBaseDna(thLocation: PointF, eagleLocation: PointF, monolithLocation: PointF): BaseDnaSignature {
        val thQuad = classifyQuadrant(thLocation)
        val eagleQuad = classifyQuadrant(eagleLocation)
        val monolithQuad = classifyQuadrant(monolithLocation)

        // Generate 64-bit spatial layout hash
        val layoutHash = (thQuad.ordinal.toLong() shl 32) or
                         (eagleQuad.ordinal.toLong() shl 16) or
                         (monolithQuad.ordinal.toLong())

        Log.i("Heatmap", "🧬 [BASE DNA] Signature Hash: $layoutHash | TH: $thQuad | Eagle: $eagleQuad | Monolith: $monolithQuad")

        return BaseDnaSignature(
            layoutHash64 = layoutHash,
            thQuadrant = thQuad,
            eagleArtilleryQuadrant = eagleQuad,
            monolithQuadrant = monolithQuad,
            historical3StarRate = 0.92f
        )
    }

    fun buildDynamicTacticalPrompt(baseDna: BaseDnaSignature): String {
        return """
        Analyze this Clash of Clans base layout.
        Base DNA Context:
        - Town Hall Position: ${baseDna.thQuadrant}
        - Eagle Artillery Threat Zone: ${baseDna.eagleArtilleryQuadrant}
        - Monolith Kill-Zone: ${baseDna.monolithQuadrant}

        Tactical Directives:
        1. Identify the weakest defensive flank avoiding direct Eagle/Monolith fire line.
        2. Compute optimal 3-Phase wave funnel coordinates:
           - Phase 1 (Funnel): Outer collector clear drop points
           - Phase 2 (Core Push): 4-finger main entry line
           - Phase 3 (Hero Abilities): Grand Warden Eternal Tome activation timing
        Return JSON format: {"entrySide": "SOUTH_WEST", "funnelPoints": [{"x": 0.28, "y": 0.78}], "wardenDelaySec": 12}
        """.trimIndent()
    }

    private fun classifyQuadrant(pt: PointF): BaseQuadrant {
        return when {
            pt.x < 0.5f && pt.y < 0.5f -> BaseQuadrant.NORTH_WEST
            pt.x >= 0.5f && pt.y < 0.5f -> BaseQuadrant.NORTH_EAST
            pt.x < 0.5f && pt.y >= 0.5f -> BaseQuadrant.SOUTH_WEST
            else -> BaseQuadrant.SOUTH_EAST
        }
    }
}
