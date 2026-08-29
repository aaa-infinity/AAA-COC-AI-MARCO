package com.cocai.autoclicker.engine

import android.graphics.PointF

/**
 * 🎯 Supercell Fixed UI Navigation Coordinates (1920x1080 Normalized)
 *
 * In Clash of Clans, all HUD buttons are permanently pinned to fixed screen anchors.
 * Using these fixed UI buttons eliminates all camera zoom/pan errors!
 */
object UniversalFixedUiMapper {

    // --- HOME VILLAGE FIXED UI ---
    val BTN_ATTACK = PointF(115f, 950f)             // Bottom-Left swords icon
    val BTN_ARMY_OVERVIEW = PointF(95f, 830f)        // Bottom-Left elixir bottle icon
    val TAB_QUICK_TRAIN = PointF(1350f, 150f)        // Army modal "Quick Train" tab
    val BTN_TRAIN_SLOT_1 = PointF(1580f, 380f)       // Quick Train Slot 1 "Train" button
    val BTN_CLOSE_MODAL = PointF(1820f, 85f)         // Top-Right "X" close modal button

    // Builder Overview (Top-Center) - Opens suggested upgrades dropdown (auto-selects Walls!)
    val BTN_BUILDER_DROPDOWN = PointF(960f, 50f)     // Top-Center builder hammer icon
    val BTN_SUGGESTED_WALL = PointF(960f, 220f)      // First suggested upgrade in dropdown (Walls)
    val BTN_UPGRADE_CONFIRM = PointF(1100f, 750f)    // Upgrade confirm button with Gold/Elixir

    // --- MATCHMAKING & ATTACK FIXED UI ---
    val BTN_FIND_MATCH = PointF(1550f, 750f)         // "Find a Match" multiplayer attack button
    val BTN_NEXT_BASE = PointF(1750f, 820f)          // "Next" matchmaking search button (Bottom-Right)
    val BTN_SURRENDER = PointF(120f, 820f)           // "Surrender / End Battle" button (Bottom-Left)
    val BTN_CONFIRM_SURRENDER = PointF(1100f, 680f)  // "Okay" confirm surrender popup
    val BTN_RETURN_HOME = PointF(960f, 920f)         // "Return Home" button on victory/loot screen

    // Hero Abilities (During Attack)
    val BTN_HERO_1_KING = PointF(300f, 980f)
    val BTN_HERO_2_QUEEN = PointF(400f, 980f)
    val BTN_HERO_3_WARDEN = PointF(500f, 980f)
    val BTN_HERO_4_CHAMPION = PointF(600f, 980f)

    // Red Line Outer Deployment Perimeter (Guaranteed legal troop drop zones)
    val DEPLOY_SOUTH_LINE_START = PointF(550f, 850f)
    val DEPLOY_SOUTH_LINE_END = PointF(1370f, 850f)
    val DEPLOY_NORTH_LINE_START = PointF(550f, 220f)
    val DEPLOY_NORTH_LINE_END = PointF(1370f, 220f)
}
