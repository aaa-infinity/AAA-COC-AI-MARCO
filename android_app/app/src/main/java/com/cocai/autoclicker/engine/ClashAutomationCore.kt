package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.ai.ApiKeyRotator
import com.cocai.autoclicker.ai.CloudVisionAgent
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import com.cocai.autoclicker.vision.OnDeviceVisionEngine

/**
 * 👑 Clash of Clans Autonomous Core Loop
 */
class ClashAutomationCore(
    private val context: Context,
    private val accessibilityService: AutoClickAccessibilityService
) {
    var isRunning: Boolean = false
        private set
    var totalRaidsCompleted: Int = 0
        private set

    val watchdog = HardwareWatchdog(context)
    val lootFarmer = AiLootFarmerEngine(accessibilityService)
    val visionEngine = OnDeviceVisionEngine()
    val keyRotator = ApiKeyRotator(context)
    val cloudAgent = CloudVisionAgent(keyRotator)

    private val handler = Handler(Looper.getMainLooper())
    var onStatusUpdate: ((String) -> Unit)? = null

    fun startLoop(accounts: Int = 1) {
        if (isRunning) return
        isRunning = true
        watchdog.startMonitoring()
        onStatusUpdate?.invoke("STARTING")
        Log.i("ClashCore", "🚀 Clash of Clans Autonomous Loot Farm Started")
        runHomeVillageSequence()
    }

    fun stopLoop() {
        isRunning = false
        watchdog.stopMonitoring()
        handler.removeCallbacksAndMessages(null)
        onStatusUpdate?.invoke("STOPPED")
        Log.i("ClashCore", "⏹ Clash of Clans Autonomous Farm Stopped")
    }

    private fun runHomeVillageSequence() {
        if (!isRunning) return

        onStatusUpdate?.invoke("CLEANUP")
        // Step 1: Dismiss Startup Popups / Defense Summary
        accessibilityService.performPercentageTap(PointF(0.500f, 0.850f)) {
            handler.postDelayed({
                // Step 2: Harvest Collectors & CC Treasury
                onStatusUpdate?.invoke("HARVEST")
                harvestVillageEconomy {
                    // Step 3: Builder Overview Wall Dump
                    onStatusUpdate?.invoke("WALL DUMP")
                    executeBuilderWallDump {
                        // Step 4: Search Matchmaking & Execute Attack
                        onStatusUpdate?.invoke("SEARCHING")
                        openMatchmaking()
                    }
                }
            }, 800L)
        }
    }

    private fun harvestVillageEconomy(onComplete: () -> Unit) {
        val collectorGrid = listOf(
            PointF(0.350f, 0.450f),
            PointF(0.650f, 0.450f),
            PointF(0.350f, 0.650f),
            PointF(0.650f, 0.650f),
            UniversalFixedUiMapper.PCT_CLAN_CASTLE_TREASURY_COLLECT
        )

        accessibilityService.performPercentageMultiTouch(collectorGrid, durationMs = 80L) {
            handler.postDelayed({ onComplete() }, 600L)
        }
    }

    private fun executeBuilderWallDump(onComplete: () -> Unit) {
        // Tap Builder Dropdown (Top-Center)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_BUILDER_DROPDOWN) {
            handler.postDelayed({
                // Tap First Recommended Upgrade (Wall)
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_BUILDER_FIRST_UPGRADE) {
                    handler.postDelayed({
                        // Tap Confirm Upgrade Button
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CONFIRM_UPGRADE_BTN) {
                            handler.postDelayed({
                                // Close Menu (tap screen center)
                                accessibilityService.performPercentageTap(PointF(0.500f, 0.500f)) {
                                    handler.postDelayed({ onComplete() }, 400L)
                                }
                            }, 500L)
                        }
                    }, 500L)
                }
            }, 500L)
        }
    }

    private fun openMatchmaking() {
        if (!isRunning) return
        // Tap Attack Button (Bottom-Left)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ATTACK_BUTTON_MAIN) {
            handler.postDelayed({
                // Tap "Find a Match" Button
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_FIND_A_MATCH_BUTTON) {
                    handler.postDelayed({
                        onStatusUpdate?.invoke("INSPECTING")
                        // Wait for Base to load then execute Loot Assault
                        handler.postDelayed({
                            executeLootAttackPhase()
                        }, 2500L)
                    }, 1200L)
                }
            }, 800L)
        }
    }

    private fun executeLootAttackPhase() {
        if (!isRunning) return
        onStatusUpdate?.invoke("RAIDING")

        lootFarmer.executeLootAssault {
            // Wait 25 seconds for loot destruction
            handler.postDelayed({
                surrenderAndReturnHome()
            }, 25000L)
        }
    }

    private fun surrenderAndReturnHome() {
        if (!isRunning) return
        onStatusUpdate?.invoke("SURRENDER")

        // Tap Surrender
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SURRENDER_BUTTON) {
            handler.postDelayed({
                // Tap Okay
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CONFIRM_SURRENDER_OK) {
                    handler.postDelayed({
                        // Tap Return Home
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_RETURN_HOME_BUTTON) {
                            totalRaidsCompleted++
                            onStatusUpdate?.invoke("VICTORY")
                            Log.i("ClashCore", "🏆 Raid #$totalRaidsCompleted Completed! Immediate next attack...")

                            // Immediate next attack (0-Training time)
                            handler.postDelayed({
                                if (isRunning) runHomeVillageSequence()
                            }, 2000L)
                        }
                    }, 1200L)
                }
            }, 600L)
        }
    }
}
