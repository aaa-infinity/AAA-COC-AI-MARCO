package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class AutoBuildingUpgraderEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isUpgrading: Boolean = false
        private set

    /**
     * Inspects builder suggestions and upgrades top suggested building
     * (Top Builder info bar at x=960, y=50)
     */
    fun upgradeSuggestedBuilding(onComplete: () -> Unit) {
        if (isUpgrading) return
        isUpgrading = true
        Log.i("BuildingUpgrader", "=== [AUTO BUILDER] Inspecting Suggested Upgrades ===")

        // Step 1: Tap Builder Icon at top of screen (x=960, y=50)
        accessibilityService.performTap(960f, 50f) {
            handler.postDelayed({
                // Step 2: Tap first suggested upgrade in dropdown list (x=960, y=140)
                accessibilityService.performTap(960f, 140f) {
                    handler.postDelayed({
                        // Step 3: Tap Upgrade Icon on selected building (x=1050, y=950)
                        accessibilityService.performTap(1050f, 950f) {
                            handler.postDelayed({
                                // Step 4: Confirm upgrade (x=1100, y=750)
                                accessibilityService.performTap(1100f, 750f) {
                                    handler.postDelayed({
                                        isUpgrading = false
                                        Log.i("BuildingUpgrader", "✓ Auto-building upgrade initiated successfully.")
                                        onComplete()
                                    }, Random.nextLong(600L, 900L))
                                }
                            }, Random.nextLong(500L, 800L))
                        }
                    }, Random.nextLong(600L, 900L))
                }
            }, Random.nextLong(700L, 1000L))
        }
    }
}
