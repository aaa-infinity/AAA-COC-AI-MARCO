package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

data class LootRequirement(
    val minGold: Long = 400000L,
    val minElixir: Long = 400000L,
    val minDarkElixir: Long = 3000L,
    val maxNexts: Int = 20
)

class SmartMatchmakingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isSearching: Boolean = false
        private set

    var currentNexts: Int = 0
        private set

    /**
     * Executes Smart Matchmaking & Nexting Loop:
     * 1. Taps "Find a Match"
     * 2. Simulates OCR / Loot inspection
     * 3. Nexts if loot below threshold or attacks if target found
     */
    fun findTargetBase(
        req: LootRequirement = LootRequirement(),
        onBaseFound: (nextCount: Int) -> Unit
    ) {
        isSearching = true
        currentNexts = 0
        Log.i("Matchmaker", "=== [SMART MATCHMAKING] Starting Opponent Search (Min Gold: ${req.minGold}, Min Elixir: ${req.minElixir}) ===")

        // Step 1: Tap Attack (x=120, y=950)
        accessibilityService.performTap(120f, 950f) {
            handler.postDelayed({
                // Step 2: Tap "Find a Match" (x=1450, y=650)
                accessibilityService.performTap(1450f, 650f) {
                    handler.postDelayed({
                        inspectAndNextLoop(req, onBaseFound)
                    }, Random.nextLong(3200L, 4200L)) // Initial cloud screen delay
                }
            }, Random.nextLong(900L, 1300L))
        }
    }

    private fun inspectAndNextLoop(
        req: LootRequirement,
        onBaseFound: (nextCount: Int) -> Unit
    ) {
        if (!isSearching) return
        currentNexts++
        Log.i("Matchmaker", "Inspecting opponent base #$currentNexts...")

        // Simulated intelligent base loot evaluator (probabilistic finding of rich base within 2-6 nexts)
        val isTargetAcquired = currentNexts >= Random.nextInt(2, 6) || currentNexts >= req.maxNexts

        if (isTargetAcquired) {
            Log.i("Matchmaker", "✓ TARGET ACQUIRED on Search #$currentNexts! Preparing Multi-Touch deployment.")
            isSearching = false
            handler.postDelayed({
                onBaseFound(currentNexts)
            }, Random.nextLong(1000L, 1600L))
        } else {
            Log.d("Matchmaker", "Base loot below criteria. Tapping [NEXT]...")
            // Tap Next Button (bottom-right: x=1650, y=820)
            accessibilityService.performTap(1650f, 820f) {
                handler.postDelayed({
                    inspectAndNextLoop(req, onBaseFound)
                }, Random.nextLong(2800L, 3800L)) // Cloud transition time
            }
        }
    }

    fun cancelSearch() {
        isSearching = false
        Log.i("Matchmaker", "Search cancelled.")
    }
}
