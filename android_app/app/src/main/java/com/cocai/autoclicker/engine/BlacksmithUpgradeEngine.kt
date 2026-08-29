package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class BlacksmithUpgradeEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isUpgradingEquipment: Boolean = false
        private set

    /**
     * Autonomous Blacksmith Hero Equipment Upgrader:
     * 1. Opens Blacksmith (x=1250, y=380)
     * 2. Selects King/Queen/Warden/RC equipment
     * 3. Upgrades equipped epic/common gear with Shiny/Glowy/Starry ores
     * 4. Closes window
     */
    fun upgradeHeroEquipment(onComplete: () -> Unit) {
        if (isUpgradingEquipment) return
        isUpgradingEquipment = true
        Log.i("Blacksmith", "=== [BLACKSMITH] Checking Hero Equipment & Ore Balances ===")

        // Step 1: Tap Blacksmith Building (x=1250, y=380)
        accessibilityService.performTap(1250f, 380f) {
            handler.postDelayed({
                // Step 2: Tap Blacksmith Upgrade Button in bottom bar (x=960, y=950)
                accessibilityService.performTap(960f, 950f) {
                    handler.postDelayed({
                        // Step 3: Tap Top Equipped Gear Slot (x=600, y=450)
                        accessibilityService.performTap(600f, 450f) {
                            handler.postDelayed({
                                // Step 4: Tap Upgrade with Ores (x=1350, y=820)
                                accessibilityService.performTap(1350f, 820f) {
                                    handler.postDelayed({
                                        // Step 5: Close Blacksmith Window (x=1820, y=85)
                                        accessibilityService.performTap(1820f, 85f) {
                                            isUpgradingEquipment = false
                                            Log.i("Blacksmith", "✓ Hero Equipment check & upgrade routine complete.")
                                            onComplete()
                                        }
                                    }, Random.nextLong(600L, 900L))
                                }
                            }, Random.nextLong(600L, 900L))
                        }
                    }, Random.nextLong(700L, 1000L))
                }
            }, Random.nextLong(800L, 1200L))
        }
    }
}
