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
     * Autonomous Wall Dump Routine:
     * When Gold/Elixir reaches high capacity, finds outer wall segments,
     * taps wall -> taps upgrade icon -> confirms with Gold/Elixir.
     */
    fun performWallUpgrades(wallsToUpgrade: Int = 3, onComplete: () -> Unit) {
        if (isUpgrading) return
        isUpgrading = true
        Log.i("WallUpgrade", "=== [AUTO WALL DUMP] Starting Wall Upgrade Routine ($wallsToUpgrade walls) ===")

        val sampleWallCoords = listOf(
            PointF(700f, 620f),
            PointF(740f, 640f),
            PointF(780f, 660f),
            PointF(1150f, 620f),
            PointF(1190f, 640f),
            PointF(1230f, 660f)
        )

        var upgraded = 0

        fun upgradeNext() {
            if (upgraded < wallsToUpgrade && upgraded < sampleWallCoords.size) {
                val wall = sampleWallCoords[upgraded++]
                Log.d("WallUpgrade", "Selecting wall piece at (${wall.x}, ${wall.y})...")

                // Step 1: Tap Wall
                accessibilityService.performTap(wall.x, wall.y) {
                    handler.postDelayed({
                        // Step 2: Tap Upgrade Button in bottom action bar (x=1050, y=950)
                        accessibilityService.performTap(1050f, 950f) {
                            handler.postDelayed({
                                // Step 3: Tap Confirm Upgrade with Gold/Elixir (x=1100, y=750)
                                accessibilityService.performTap(1100f, 750f) {
                                    handler.postDelayed({
                                        upgradeNext()
                                    }, Random.nextLong(450L, 700L))
                                }
                            }, Random.nextLong(400L, 600L))
                        }
                    }, Random.nextLong(400L, 600L))
                }
            } else {
                isUpgrading = false
                Log.i("WallUpgrade", "Wall upgrade routine completed ($upgraded walls upgraded).")
                onComplete()
            }
        }

        upgradeNext()
    }
}
