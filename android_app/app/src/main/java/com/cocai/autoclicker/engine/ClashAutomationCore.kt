package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 👑 CLASH AUTOMATION CORE ENGINE - PURE DRAGON FARMING EDITION
 *
 * Dedicated Capabilities:
 * - 🐉 Pure Zap Dragon & Mass Air Loot Farming Strategy
 * - 🏠 Pure Home Village Focus (No Builder Base Clutter)
 * - 🧱 Smart Builder Overview Wall Dump (Never waste loot)
 * - ⚡ Snapshot Matchmaking Fast-Skipper (HSV instant detection)
 * - 🤖 2-Way Conversational Telegram Remote Control (/status, /pause, /resume, /attack, /walls, /schedule)
 * - ⏰ Smart Sleep / Schedule Timer (Automated farming hours & rest breaks)
 * - 🔄 Multi-Account Supercell ID Auto-Cycle (2-4 Accounts)
 * - 🔋 Battery & Thermal Watchdog (> 42°C / < 15% Safe Halts)
 * - 🛡️ 12-Min CC Reinforcement Auto-Requester
 * - 💎 Obstacle & Gem Box Auto-Clearer
 * - 🚨 Panic Stop: Hardware Volume Down Override
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
    val watchdog = DeviceHealthWatchdog(context)
    val accountSwitcher = SupercellIdAccountSwitcher(accessibilityService)
    val ccRequester = ClanCastleAutoRequester(accessibilityService)
    val freebieCollector = DailyFreebieCollector(accessibilityService)
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
            pauseForCooldown(cooldownMs = 600000L) // 10 minutes cooldown
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
                👑 <b>Ai Marco coc - Dragon Farming Status</b>
                • <b>State:</b> ${if (isRunning) "🟢 RUNNING & FARMING" else "⏸ PAUSED / IDLE"}
                • <b>Strategy:</b> 🐉 Pure Zap Dragon Farming
                • <b>Active Account:</b> #${acc} of $totalAccounts
                • <b>Total Raids:</b> $totalRaidsCompleted Raids
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
                continueVillageUpgradesAndArmy()
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
        updateStatus("🚀 [STARTING] Standardizing Camera View...")

        zoomOutAndResetCamera {
            executeVillageRoutine()
        }
    }

    fun stopLoop() {
        isRunning = false
        watchdog.stopMonitoring()
        handler.removeCallbacksAndMessages(null)
        updateStatus("⏸ [PAUSED] Idle")
    }

    /**
     * 1. Standardize Camera Zoom (Pinch Out)
     */
    private fun zoomOutAndResetCamera(onComplete: () -> Unit) {
        accessibilityService.performPinchZoomOut(durationMs = 450L) {
            handler.postDelayed({
                if (isRunning) onComplete()
            }, jitterCalibrator.getHumanizedDelayMs(800L))
        }
    }

    /**
     * 2. Home Village Routine: Freebies -> CC Request -> Walls -> Quick Train Dragons -> Attack
     */
    private fun executeVillageRoutine() {
        if (!isRunning) return

        // Check Smart Sleep / Schedule Window
        if (!smartSchedule.isWithinScheduleWindow()) {
            updateStatus("🌙 [SLEEP SCHEDULE] Outside farming hours. Sleeping 15m...")
            pauseForCooldown(cooldownMs = 900000L)
            return
        }

        // Daily Merchant Freebies
        freebieCollector.collectIfDue {
            // Clan Castle Check every 3 raids
            if (totalRaidsCompleted % 3 == 0) {
                ccRequester.requestReinforcements {
                    continueVillageUpgradesAndArmy()
                }
            } else {
                continueVillageUpgradesAndArmy()
            }
        }
    }

    private fun continueVillageUpgradesAndArmy() {
        updateStatus("🧱 [BUILDER OVERVIEW] Upgrading Walls with Free Builder...")

        // Open Builder Overview (Top-Center)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_BUILDER_DROPDOWN) {
            handler.postDelayed({
                // Tap Suggested Wall in Dropdown
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SUGGESTED_WALL) {
                    handler.postDelayed({
                        // Tap Confirm Upgrade with Gold/Elixir
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_UPGRADE_CONFIRM) {
                            handler.postDelayed({
                                // Train 0-Cost Dragon Army next
                                trainDragonArmyRoutine {
                                    startMatchmakingAndAttack()
                                }
                            }, 600L)
                        }
                    }, 600L)
                }
            }, 800L)
        }
    }

    /**
     * 3. Army Routine: Double-Queue 0-Cost Dragon Army (Slot 1)
     */
    private fun trainDragonArmyRoutine(onComplete: () -> Unit) {
        if (!isRunning) return
        updateStatus("🐉 [QUICK TRAIN] Double-Queuing 0-Cost Dragon Army...")

        // Tap Bottom-Left Army Bottle Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ARMY_OVERVIEW) {
            handler.postDelayed({
                // Tap Quick Train Tab
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_QUICK_TRAIN_TAB) {
                    handler.postDelayed({
                        // Tap Train Slot 1 (Zap Dragons)
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_TRAIN_SLOT_1) {
                            handler.postDelayed({
                                // Tap Close X
                                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_CLOSE_MODAL) {
                                    handler.postDelayed({
                                        if (isRunning) onComplete()
                                    }, 700L)
                                }
                            }, 500L)
                        }
                    }, 600L)
                }
            }, 800L)
        }
    }

    /**
     * 4. Attack Routine: Search Match -> Nexting -> Dragon Assault
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
            // Target Base Selected! Execute Pure Dragon Assault
            executeDragonFarmingAssault()
        }
    }

    /**
     * 5. Pure Dragon Army Assault (Zap Air Defenses + Mass Dragon Line + Warden)
     */
    private fun executeDragonFarmingAssault() {
        if (!isRunning) return
        updateStatus("🐉 [DRAGON ASSAULT] Deploying Zap Dragons & Heroes...")

        val startLine = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START
        val endLine = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END

        dragonEngine.executeDragonAssault(startLine, endLine) {
            updateStatus("🔥 [DRAGON BATTLE] Dragons destroying storages & collectors...")
            handler.postDelayed({
                finishAttackAndReturnHome()
            }, 26000L) // 26 seconds of destruction
        }
    }

    /**
     * 6. End Battle -> Return Home -> Multi-Account Check -> Loop!
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
        // Multi-Account Supercell ID Rotation: Every 3 raids across configured accounts
        if (totalAccounts > 1 && totalRaidsCompleted % 3 == 0) {
            updateStatus("🔄 [SWITCHING] Rotating to next Supercell ID account...")
            accountSwitcher.switchToNextAccount(totalAccounts) {
                executeVillageRoutine()
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
                    zoomOutAndResetCamera {
                        executeVillageRoutine()
                    }
                }
            }, 6000L)
        }
    }

    /**
     * Handles "Personal Break" / "Village Under Attack" 15-Minute Cooldown
     */
    fun triggerPersonalBreakCooldown() {
        updateStatus("⏳ [COOLDOWN] Personal Break / Under Attack (15m)...")
        telegramBot.sendMessage("⏳ <b>[PERSONAL BREAK]</b> Village Under Attack / Personal Break detected. Pausing for 15 minutes.")
        pauseForCooldown(cooldownMs = 900000L)
    }

    private fun pauseForCooldown(cooldownMs: Long) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isRunning) {
                updateStatus("🚀 [RESUMING] Cooldown ended. Resuming farm loop...")
                zoomOutAndResetCamera {
                    executeVillageRoutine()
                }
            }
        }, cooldownMs)
    }
}
