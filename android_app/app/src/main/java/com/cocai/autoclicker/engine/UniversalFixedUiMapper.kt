package com.cocai.autoclicker.engine

import android.graphics.PointF

/**
 * 🗺️ Universal Normalized Percentage Coordinates (0.0f - 1.0f)
 * Works flawlessly across every phone/tablet resolution and aspect ratio.
 */
object UniversalFixedUiMapper {
    // ⚔️ Matchmaking & Combat Navigation
    val PCT_ATTACK_BUTTON_MAIN = PointF(0.062f, 0.908f)
    val PCT_FIND_A_MATCH_BUTTON = PointF(0.825f, 0.695f)
    val PCT_NEXT_BUTTON_BATTLE = PointF(0.920f, 0.815f)
    val PCT_SURRENDER_BUTTON = PointF(0.065f, 0.835f)
    val PCT_CONFIRM_SURRENDER_OK = PointF(0.605f, 0.625f)
    val PCT_RETURN_HOME_BUTTON = PointF(0.500f, 0.875f)
    val PCT_BATTLE_FAST_FORWARD = PointF(0.920f, 0.080f)

    // 🛡️ Home Village Economy & Builder Overview
    val PCT_BUILDER_DROPDOWN = PointF(0.500f, 0.048f)
    val PCT_BUILDER_FIRST_UPGRADE = PointF(0.500f, 0.205f)
    val PCT_CONFIRM_UPGRADE_BTN = PointF(0.575f, 0.695f)
    val PCT_CLAN_CASTLE_TREASURY_COLLECT = PointF(0.500f, 0.750f)

    // 🐉 Army Deployment Trays
    val PCT_TROOP_SLOT_1 = PointF(0.080f, 0.900f)
    val PCT_SPELL_SLOT_1 = PointF(0.280f, 0.900f)
    val PCT_HERO_1_KING = PointF(0.420f, 0.900f)
    val PCT_HERO_2_QUEEN = PointF(0.500f, 0.900f)
    val PCT_HERO_3_WARDEN = PointF(0.580f, 0.900f)

    // 📐 Deployment Boundaries
    val PCT_DEPLOY_SOUTH_START = PointF(0.200f, 0.820f)
    val PCT_DEPLOY_SOUTH_END = PointF(0.800f, 0.820f)
}
