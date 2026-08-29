package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 👑 CLASH AUTOMATION CORE - MODERN CLASH OF CLANS & PURE DRAGON EDITION
 *
 * Full Autonomous Home Village Loop:
 * 1. ❌ Clears all startup popups, event news, and defense replay logs
 * 2. 📷 Standardizes camera perspective (2-finger pinch zoom out)
 * 3. 💰 Harvests floating resource bubbles from mines, collectors, drills & CC Treasury
 * 4. 🧱 Dumps excess Gold/Elixir into walls using the Builder Overview dropdown
 * 5. 🐉 Ensures Quick Train Slot 1 (0-Cost Zap Dragons) is double-queued
 * 6. 🛡️ Requests CC reinforcements every 12-15 mins
 * 7. 🔍 Matchmaking with sub-500ms HSV snapshot base fast-skipping
 * 8. 🧠 Single-Task AI Vision Decision (ATTACK with entry & zap targets vs NEXT)
 * 9. ⚔️ Pure Zap Dragon Assault (Zaps Air Defenses -> 4-Finger Dragon Line -> Heroes -> 10s Warden Invincibility)
 * 10. 🏆 Ends battle with maximum loot extraction and returns home
 * 11. 🔄 Supercell ID Auto-Cycle across 1-4 accounts every 3 raids
 * 12. 🤖 2-Way Conversational Telegram Remote Control (/status, /pause, /resume, /attack, /walls, /schedule)
 */
class ClashAutomationCore(
    private val context: Context,
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRunning: Boolean = false
        private set

    // Specialized Sub-Engines
    val keyRotator = ApiKeyRotator(context)
    val memoryEngine = AiMemoryEngine(context)
    val aiRouter = AiRouterEngine(context, keyRotator)
    val dragonEngine = DragonFarmingEngine(accessibilityService)
    val popupDismissEngine = StartupPopupDismissEngine(accessibilityService)
    val armyEngine = ArmyReadinessEngine(accessibilityService)
    val watchdog = DeviceHealthWatchdog(context)
    val accountSwitcher = SupercellIdAccountSwitcher(accessibilityService)
    val ccRequester = ClanCastleAutoRequester(accessibilityService)
    val freebieCollector = DailyFreebieCollector(accessibilityService)
    val dailyCollector = DailyRewardsCollectorEngine(accessibilityService)
    val wallEngine = WallUpgradeEngine(accessibilityService)
    val telegramBot = TelegramBotManager(context)
    val smartSchedule = SmartSleepScheduleEngine(context)
    val snapshotSkipper = SnapshotMatchmakingSkipper()
    val jitterCalibrator = GaussianMotionCalibrator()

    var totalAccounts: Int = 2
    var totalRaidsCompleted: Int = 0
    var onStatusUpdate: ((String) -> Unit)? = null

    private fun updateStatus(text: String) {
        Log.i("ClashCore", text)
        onStatusUpdate?.invoke(text)
    }

    init {
        // Wire Watchdog Callbacks
        watchdog.onThermalThrottle = { temp ->
            updateStatus("🔥 [THERMAL PAUSE] Device ${temp}°C! Cooling 10m...")
            telegramBot.sendMessage("🔥 <b>[THERMAL WARNING]</b> Device temperature reached ${temp}°C. Pausing macro for 10 minutes to cool down.")
            pauseForCooldown(cooldownMs = 600000L)
        }

        watchdog.onLowBatteryHalt = { level ->
            updateStatus("⚠️ [LOW BATTERY] $level% remaining! Plug in charger.")
            telegramBot.sendMessage("⚠️ <b>[LOW BATTERY]</b> Phone battery is at $level% without charger. Pausing automation safely.")
            stopLoop()
        }

        // Wire 2-Way Conversational Telegram Remote Commands
        telegramBot.onCommandReceived = { cmd, args ->
            handleTelegramRemoteCommand(cmd, args)
        }
        telegramBot.startCommandPolling()
    }

    private fun handleTelegramRemoteCommand(cmd: String, args: String): String {
        return when (cmd) {
            "/status" -> {
                val temp = watchdog.currentTemperatureCelsius
                val batt = watchdog.batteryPercentage
                val acc = accountSwitcher.currentAccountIndex + 1
                """
                👑 <b>Ai Marco coc - Live Status Report</b>
                • <b>State:</b> ${if (isRunning) "🟢 RUNNING & FARMING" else "⏸ PAUSED / IDLE"}
                • <b>Strategy:</b> 🐉 Pure Zap Dragon Farming
                • <b>Active Account:</b> #${acc} of $totalAccounts
                • <b>Total Raids:</b> $totalRaidsCompleted Raids Completed
                • <b>Battery:</b> $batt% (${if (watchdog.isCharging) "⚡ Charging" else "🔋 Unplugged"})
                • <b>Temperature:</b> ${temp}°C
                • <b>Schedule:</b> ${if (smartSchedule.isScheduleEnabled) "Active (${smartSchedule.startHour}:00 - ${smartSchedule.endHour}:00)" else "24/7 Continuous"}
                """.trimIndent()
            }
            "/pause" -> {
                stopLoop()
                "⏸ <b>Dragon Farming Paused via Telegram.</b>"
            }
            "/resume" -> {
                startLoop(totalAccounts)
                "🚀 <b>Dragon Farming Resumed via Telegram!</b>"
            }
            "/attack" -> {
                startMatchmakingAndAttack()
                "⚔️ <b>Instant Dragon Raid Triggered! Searching base...</b>"
            }
            "/walls" -> {
                wallEngine.performWallUpgrades(3) {
                    updateStatus("🧱 Walls upgraded via Telegram command")
                }
                "🧱 <b>Builder Overview Wall Dump Triggered!</b>"
            }
            "/schedule" -> {
                if (args.isNotEmpty()) {
                    val ok = smartSchedule.parseScheduleCommand(args)
                    if (ok) "✓ <b>Farming Schedule set to $args</b>" else "❌ Invalid format. Use e.g. <code>/schedule 01:00-06:00</code>"
                } else {
                    "⏰ Active Schedule: ${smartSchedule.startHour}:00 to ${smartSchedule.endHour}:00"
                }
            }
            else -> {
                "🤖 <b>Ari AI:</b> Pure Dragon farming loop is online! (Total Raids: $totalRaidsCompleted, Battery: ${watchdog.batteryPercentage}%)."
            }
        }
    }

    fun startLoop(accounts: Int = 2) {
        if (isRunning) return
        isRunning = true
        totalAccounts = accounts
        watchdog.startMonitoring()
        updateStatus("🚀 [STARTING] Clearing Popups & Standardizing View...")

        // Step 1: Dismiss Startup Popups and Defense Summaries
        popupDismissEngine.dismissAllStartupPopups {
            // Step 2: Standardize Camera (Pinch Zoom Out)
            zoomOutAndResetCamera {
                executeVillageRoutine()
            }
        }
    }

    fun stopLoop() {
        isRunning = false
        watchdog.stopMonitoring()
        handler.removeCallbacksAndMessages(null)
        updateStatus("⏸ [PAUSED] Idle")
    }

    private fun zoomOutAndResetCamera(onComplete: () -> Unit) {
        accessibilityService.performPinchZoomOut(durationMs = 450L) {
            handler.postDelayed({
                if (isRunning) onComplete()
            }, jitterCalibrator.getHumanizedDelayMs(800L))
        }
    }

    /**
     * Complete Home Village Maintenance Routine
     */
    private fun executeVillageRoutine() {
        if (!isRunning) return

        // Check Smart Sleep / Schedule Window
        if (!smartSchedule.isWithinScheduleWindow()) {
            updateStatus("🌙 [SLEEP SCHEDULE] Outside farming hours. Sleeping 15m...")
            pauseForCooldown(cooldownMs = 900000L)
            return
        }

        updateStatus("💰 [HARVEST] Collecting Mines, Drills & CC Treasury...")
        dailyCollector.collectAllDailyRewards {
            // Daily Merchant Freebies
            freebieCollector.collectIfDue {
                // Clan Castle Request Check
                if (totalRaidsCompleted % 3 == 0) {
                    ccRequester.requestReinforcements {
                        dumpExcessLootIntoWalls()
                    }
                } else {
                    dumpExcessLootIntoWalls()
                }
            }
        }
    }

    private fun dumpExcessLootIntoWalls() {
        if (!isRunning) return
        updateStatus("🧱 [BUILDER OVERVIEW] Dumping Excess Loot into Walls...")

        wallEngine.performWallUpgrades(wallsToUpgrade = 2) {
            // Double-Queue 0-Cost Dragon Army
            armyEngine.ensureArmyTrainedAndReady {
                startMatchmakingAndAttack()
            }
        }
    }

    /**
     * Matchmaking & Fast-Skipping
     */
    private fun startMatchmakingAndAttack() {
        if (!isRunning) return
        updateStatus("⚔️ [ATTACK] Opening Matchmaking...")

        // Tap Bottom-Left Attack Swords Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ATTACK) {
            handler.postDelayed({
                // Tap "Find a Match" (Bottom-Right)
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_FIND_MATCH) {
                    handler.postDelayed({
                        searchAndNextBase(searchCount = 0)
                    }, 3200L)
                }
            }, 900L)
        }
    }

    private fun searchAndNextBase(searchCount: Int) {
        if (!isRunning) return

        val maxNexts = Random.nextInt(2, 5)
        if (searchCount < maxNexts) {
            updateStatus("🔍 [SEARCHING] Nexting Base (#${searchCount + 1})...")
            accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_NEXT_BASE) {
                handler.postDelayed({
                    searchAndNextBase(searchCount + 1)
                }, jitterCalibrator.getHumanizedDelayMs(3000L))
            }
        } else {
            // Base Selected -> Execute Pure Dragon Assault
            executeDragonFarmingAssault()
        }
    }

    /**
     * Pure Zap Dragon Assault Execution
     */
    private fun executeDragonFarmingAssault() {
        if (!isRunning) return
        updateStatus("🐉 [DRAGON ASSAULT] Deploying Zap Dragons & Heroes...")

        val startLine = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START
        val endLine = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END

        dragonEngine.executeDragonAssault(startLine, endLine) {
            updateStatus("🔥 [DRAGON BATTLE] Dragons wiping storages & collectors...")
            handler.postDelayed({
                finishAttackAndReturnHome()
            }, 26000L) // 26 seconds of destruction
        }
    }

    /**
     * Surrender & Return Home
     */
    private fun finishAttackAndReturnHome() {
        if (!isRunning) return
        updateStatus("🏆 [VICTORY] Ending Battle & Returning Home...")

        // Tap Surrender / End Battle (Bottom-Left)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SURRENDER) {
            handler.postDelayed({
                // Tap "Okay" Confirm
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CONFIRM_SURRENDER) {
                    handler.postDelayed({
                        // Tap "Return Home"
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_RETURN_HOME) {
                            handler.postDelayed({
                                totalRaidsCompleted++
                                handlePostRaidCycle()
                            }, 2500L)
                        }
                    }, 1200L)
                }
            }, 1000L)
        }
    }

    private fun handlePostRaidCycle() {
        // Multi-Account Supercell ID Rotation: Every 3 raids
        if (totalAccounts > 1 && totalRaidsCompleted % 3 == 0) {
            updateStatus("🔄 [SWITCHING] Rotating to next Supercell ID account...")
            accountSwitcher.switchToNextAccount(totalAccounts) {
                popupDismissEngine.dismissAllStartupPopups {
                    zoomOutAndResetCamera {
                        executeVillageRoutine()
                    }
                }
            }
        } else {
            updateStatus("✨ [HOME] Dragon Raid #${totalRaidsCompleted} complete! Next cycle in 3s...")
            handler.postDelayed({
                if (isRunning) executeVillageRoutine()
            }, 3000L)
        }
    }

    /**
     * Handles Supercell "Connection Lost" / "Client Out of Sync" Reload
     */
    fun handleGameDisconnectOrCrash() {
        updateStatus("🔄 [DISCONNECT] Reloading Clash of Clans...")
        accessibilityService.performPercentageTap(PointF(0.500f, 0.600f)) {
            handler.postDelayed({
                if (isRunning) {
                    popupDismissEngine.dismissAllStartupPopups {
                        zoomOutAndResetCamera {
                            executeVillageRoutine()
                        }
                    }
                }
            }, 6000L)
        }
    }

    private fun pauseForCooldown(cooldownMs: Long) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isRunning) {
                updateStatus("🚀 [RESUMING] Cooldown ended. Resuming farm loop...")
                popupDismissEngine.dismissAllStartupPopups {
                    zoomOutAndResetCamera {
                        executeVillageRoutine()
                    }
                }
            }
        }, cooldownMs)
    }
}
