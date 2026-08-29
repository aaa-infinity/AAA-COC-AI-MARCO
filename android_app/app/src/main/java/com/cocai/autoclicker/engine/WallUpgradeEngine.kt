package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class WallUpgradeEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isUpgrading: Boolean = false
        private set

    /**
     * Dedicated Full Loot Wall Dump Engine (Uses 1 Dedicated Free Builder):
     * When Gold/Elixir storages fill up from farming, this routine dumps all excess
     * resources into upgrading walls piece-by-piece and row-by-row.
     */
    fun performWallUpgrades(wallsToUpgrade: Int = 3, onComplete: () -> Unit) {
        if (isUpgrading) return
        isUpgrading = true
        Log.i("WallUpgrade", "=== [DEDICATED WALL DUMP] Burning Full Farmed Loot into Wall Upgrades ($wallsToUpgrade walls) ===")

        val sampleWallCoords = listOf(
            PointF(700f, 620f),
            PointF(740f, 640f),
            PointF(780f, 660f),
            PointF(820f, 680f),
            PointF(1150f, 620f),
            PointF(1190f, 640f),
            PointF(1230f, 660f),
            PointF(1270f, 680f)
        )

        var upgraded = 0

        fun upgradeNext() {
            if (upgraded < wallsToUpgrade && upgraded < sampleWallCoords.size) {
                val wall = sampleWallCoords[upgraded++]
                Log.d("WallUpgrade", "Selecting wall piece at (${wall.x}, ${wall.y}) to upgrade...")

                // Step 1: Tap Wall
                accessibilityService.performTap(wall.x, wall.y) {
                    handler.postDelayed({
                        // Step 2: Tap Upgrade Button in bottom action bar (x=1050, y=950)
                        accessibilityService.performTap(1050f, 950f) {
                            handler.postDelayed({
                                // Step 3: Tap Confirm Upgrade with Gold or Elixir (x=1100, y=750)
                                accessibilityService.performTap(1100f, 750f) {
                                    handler.postDelayed({
                                        // Tap outside to deselect
                                        accessibilityService.performTap(960f, 300f) {
                                            handler.postDelayed({
                                                upgradeNext()
                                            }, Random.nextLong(300L, 500L))
                                        }
                                    }, Random.nextLong(350L, 550L))
                                }
                            }, Random.nextLong(350L, 550L))
                        }
                    }, Random.nextLong(350L, 550L))
                }
            } else {
                isUpgrading = false
                Log.i("WallUpgrade", "✓ Wall dump complete ($upgraded walls upgraded with free builder).")
                onComplete()
            }
        }

        upgradeNext()
    }
}
