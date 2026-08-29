package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 👑 CLASH AUTOMATION CORE ENGINE - COGNITIVE PEAK EDITION
 *
 * Integrated Capabilities:
 * - Multi-Provider Vision Auto-Failover Router (Gemini -> OpenRouter -> Groq -> Local)
 * - Tactical Defense Heatmap & 64-Bit Base DNA Analyzer
 * - 3-Phase Wave-Based Funnel & Hero Equipment Orchestrator
 * - Fixed-UI Percentage State Machine
 * - Top Builder Overview Wall Dump
 * - Multi-Account Supercell ID Auto-Cycle (2-4 Accounts)
 * - Personal Break / Under Attack 15-min Cooldown Detector
 * - Battery & Thermal Watchdog (> 42°C / < 15% Safe Halts)
 * - Clan Castle 12-min Troop Auto-Requester
 * - Clan Capital Weekend & Clan Games Task Automator
 * - Daily Merchant Freebie & Magic Snack Claimer
 * - 5th Hero (Dragon Duke) + Greedy Raven Pet Ability Trigger
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
    val heatmapAnalyzer = DefenseHeatmapAnalyzer(memoryEngine)
    val waveOrchestrator = TacticalWaveOrchestrator(accessibilityService)
    val watchdog = DeviceHealthWatchdog(context)
    val accountSwitcher = SupercellIdAccountSwitcher(accessibilityService)
    val ccRequester = ClanCastleAutoRequester(accessibilityService)
    val capitalEngine = ClanCapitalEngine(accessibilityService)
    val gamesEngine = ClanGamesEngine(accessibilityService)
    val freebieCollector = DailyFreebieCollector(accessibilityService)
    val dragonDuke = DragonDukeManager(accessibilityService)
    val attackSafety = AttackSafetyEngine()
    val chatRecruiter = GlobalChatEngine(accessibilityService)

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
            pauseForCooldown(cooldownMs = 600000L) // 10 minutes cooldown
        }

        watchdog.onLowBatteryHalt = { level ->
            updateStatus("⚠️ [LOW BATTERY] $level% remaining! Plug in charger.")
            stopLoop()
        }
    }

    fun startLoop(accounts: Int = 2) {
        if (isRunning) return
        isRunning = true
        totalAccounts = accounts
        watchdog.startMonitoring()
        updateStatus("🚀 [STARTING] Standardizing Camera View...")

        // Step 1: Smooth 2-finger zoom out
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
            }, 800L)
        }
    }

    /**
     * 2. Village Routine: Daily Freebies -> Clan Games -> Walls -> Army -> Attack
     */
    private fun executeVillageRoutine() {
        if (!isRunning) return

        // Step A: Daily Merchant Freebies (once per 24h)
        freebieCollector.collectIfDue {
            // Step B: Clan Games / Clan Castle Check
            if (totalRaidsCompleted % 3 == 0) {
                ccRequester.requestReinforcements {
                    gamesEngine.checkAndManageClanGames {
                        continueVillageUpgradesAndArmy()
                    }
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
                                // Train Army next
                                trainArmyRoutine {
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
     * 3. Army Routine: Double-Queue 0-Cost Pro Meta Army
     */
    private fun trainArmyRoutine(onComplete: () -> Unit) {
        if (!isRunning) return
        updateStatus("⚡ [QUICK TRAIN] Double-Queuing 0-Cost Army...")

        // Tap Bottom-Left Army Bottle Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ARMY_OVERVIEW) {
            handler.postDelayed({
                // Tap Quick Train Tab
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_QUICK_TRAIN_TAB) {
                    handler.postDelayed({
                        // Tap Train Slot 1
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
     * 4. Attack Routine: Search Match -> Smart Nexting -> Deploy
     */
    private fun startMatchmakingAndAttack() {
        if (!isRunning) return
        updateStatus("⚔️ [ATTACK] Opening Matchmaking...")

        // Tap Bottom-Left Attack Swords Icon
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_ATTACK) {
            handler.postDelayed({
                // Tap "Find a Match" (Bottom-Right)
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_FIND_MATCH) {
                    // Allow 3.5s for matchmaking clouds to clear
                    handler.postDelayed({
                        searchAndNextBase(searchCount = 0)
                    }, 3500L)
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
                }, Random.nextLong(2800L, 3800L))
            }
        } else {
            // Target Base Selected! Execute 3-Phase Funnel Attack Orchestration
            execute3PhaseWaveAssault()
        }
    }

    /**
     * 5. 3-Phase Wave Funnel Assault + 5th Hero (Dragon Duke) Dispatch
     */
    private fun execute3PhaseWaveAssault() {
        if (!isRunning) return
        updateStatus("🌊 [WAVE ASSAULT] 3-Phase Funnel & Hero Equipment Orchestration...")

        val startLine = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START
        val endLine = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END

        waveOrchestrator.execute3PhaseWaveAttack(startLine, endLine, wardenDelaySec = 12) {
            // Deploy 5th Hero (Dragon Duke) + Greedy Raven Pet
            dragonDuke.deployAndTrigger5thHero(PointF(0.500f, 0.785f)) {
                updateStatus("🛡️ [BATTLE] Core Charge (Warden Tome + Dragon Duke Active)...")
                handler.postDelayed({
                    finishAttackAndReturnHome()
                }, 28000L)
            }
        }
    }

    /**
     * 6. End Battle -> Return to Village -> Account Rotation Check -> Loop!
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
            updateStatus("✨ [HOME] Raid #${totalRaidsCompleted} complete! Next cycle in 3s...")
            handler.postDelayed({
                if (isRunning) executeVillageRoutine()
            }, 3000L)
        }
    }

    /**
     * Handles "Personal Break" / "Village Under Attack" 15-Minute Cooldown
     */
    fun triggerPersonalBreakCooldown() {
        updateStatus("⏳ [COOLDOWN] Personal Break / Under Attack (15m)...")
        pauseForCooldown(cooldownMs = 900000L) // 15 minutes = 900,000 ms
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
