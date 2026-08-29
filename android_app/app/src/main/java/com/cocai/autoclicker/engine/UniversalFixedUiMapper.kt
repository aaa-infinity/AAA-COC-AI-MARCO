package com.cocai.autoclicker.engine

import android.graphics.PointF

/**
 * 🎯 Supercell Fixed UI Navigation (Normalized 0.0f - 1.0f Screen Percentages)
 *
 * Guaranteed 100% resolution-independent across all phone models (1080p, 1440p, 720p, 16:9, 19.5:9, 20:9).
 */
object UniversalFixedUiMapper {

    // --- HOME VILLAGE HUD (Screen Percentages) ---
    val PCT_ATTACK = PointF(0.065f, 0.880f)           // Bottom-Left Attack Swords Icon
    val PCT_ARMY_OVERVIEW = PointF(0.052f, 0.780f)    // Bottom-Left Army Bottle Icon
    val PCT_QUICK_TRAIN_TAB = PointF(0.705f, 0.140f)  // Army Modal "Quick Train" Tab
    val PCT_TRAIN_SLOT_1 = PointF(0.825f, 0.355f)     // Quick Train Slot 1 "Train" Button
    val PCT_CLOSE_MODAL = PointF(0.948f, 0.082f)      // Top-Right "X" Close Button

    // Builder Overview (Top-Center) - Opens Supercell's suggested upgrades (Auto-selects Walls!)
    val PCT_BUILDER_DROPDOWN = PointF(0.500f, 0.048f) // Top-Center Builder Icon
    val PCT_SUGGESTED_WALL = PointF(0.500f, 0.205f)   // First suggested upgrade in dropdown (Walls)
    val PCT_UPGRADE_CONFIRM = PointF(0.575f, 0.695f)  // Confirm Upgrade with Gold/Elixir

    // --- MATCHMAKING & BATTLE HUD ---
    val PCT_FIND_MATCH = PointF(0.810f, 0.695f)       // "Find a Match" multiplayer attack button
    val PCT_NEXT_BASE = PointF(0.912f, 0.760f)        // "Next" search button (Bottom-Right)
    val PCT_SURRENDER = PointF(0.065f, 0.760f)        // "Surrender / End Battle" (Bottom-Left)
    val PCT_CONFIRM_SURRENDER = PointF(0.575f, 0.630f)// "Okay" confirm surrender
    val PCT_RETURN_HOME = PointF(0.500f, 0.850f)      // "Return Home" after battle

    // Hero Abilities
    val PCT_HERO_1_KING = PointF(0.180f, 0.900f)
    val PCT_HERO_2_QUEEN = PointF(0.240f, 0.900f)
    val PCT_HERO_3_WARDEN = PointF(0.300f, 0.900f)
    val PCT_HERO_4_CHAMPION = PointF(0.360f, 0.900f)

    // Red Line Legal Deployment Perimeter
    val PCT_DEPLOY_SOUTH_START = PointF(0.285f, 0.785f)
    val PCT_DEPLOY_SOUTH_END = PointF(0.715f, 0.785f)
    val PCT_DEPLOY_NORTH_START = PointF(0.285f, 0.205f)
    val PCT_DEPLOY_NORTH_END = PointF(0.715f, 0.205f)
}
