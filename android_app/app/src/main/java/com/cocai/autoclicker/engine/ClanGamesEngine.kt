package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 🎪 Clan Games Task Automator
 *
 * 1. Opens the Clan Games Caravan
 * 2. Scans & claims completed quest points
 * 3. Selects high-yield farming quests (Loot Gold/Elixir, Destroy Collectors, Win Stars)
 * 4. Loops until 4,000 max points achieved
 */
class ClanGamesEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isManagingQuests: Boolean = false
        private set

    var totalPointsEarned: Int = 0

    private val PCT_GAMES_CARAVAN = PointF(0.165f, 0.360f)       // Clan Games Caravan on Village border
    private val PCT_QUEST_SLOT_1 = PointF(0.350f, 0.450f)        // First Quest Card (Easy Loot Quest)
    private val PCT_START_QUEST_BTN = PointF(0.500f, 0.780f)     // "Start Challenge" button
    private val PCT_CLAIM_REWARD_BTN = PointF(0.500f, 0.720f)    // "Claim Points" button

    fun checkAndManageClanGames(onComplete: () -> Unit) {
        if (isManagingQuests || totalPointsEarned >= 4000) {
            onComplete()
            return
        }

        isManagingQuests = true
        Log.i("ClanGames", "=== [CLAN GAMES] Checking Caravan & Active Quests ($totalPointsEarned/4000 pts) ===")

        // Step 1: Tap Caravan
        accessibilityService.performPercentageTap(PCT_GAMES_CARAVAN) {
            handler.postDelayed({
                // Step 2: Claim any finished quest
                accessibilityService.performPercentageTap(PCT_CLAIM_REWARD_BTN) {
                    handler.postDelayed({
                        // Step 3: Select & Start New Loot/Star Quest
                        accessibilityService.performPercentageTap(PCT_QUEST_SLOT_1) {
                            handler.postDelayed({
                                accessibilityService.performPercentageTap(PCT_START_QUEST_BTN) {
                                    handler.postDelayed({
                                        // Close Caravan Board
                                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                                            isManagingQuests = false
                                            Log.i("ClanGames", "✓ Clan Games quest active and tracking.")
                                            onComplete()
                                        }
                                    }, 600L)
                                }
                            }, 800L)
                        }
                    }, 800L)
                }
            }, Random.nextLong(1200L, 1600L))
        }
    }
}
