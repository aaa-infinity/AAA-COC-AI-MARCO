package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 👑 CLASH AUTOMATION CORE ENGINE - ULTIMATE PRO SUITE
 *
 * Integrated Capabilities:
 * - Fixed-UI Percentage State Machine
 * - Top Builder Overview Wall Dump
 * - Multi-Account Supercell ID Auto-Cycle (2-4 Accounts)
 * - Personal Break / Under Attack 15-min Cooldown Detector
 * - Battery & Thermal Watchdog (> 42°C / < 15% Safe Halts)
 * - Clan Castle 12-min Troop Auto-Requester
 * - Clan Capital Weekend & Clan Games Task Automator
 * - Daily Merchant Freebie & Magic Snack Claimer
 * - 5th Hero (Dragon Duke) + Greedy Raven Pet Ability Trigger
 * - 4-Finger Red-Line Deployment with Warden Eternal Tome Invincibility
 */
class ClashAutomationCore(
    private val context: Context,
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRunning: Boolean = false
        private set

    // Specialized Sub-Engines
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
            // Target Base Selected! Execute 4-Finger Red Line Deployment
            executeRedLineDeployment()
        }
    }

    /**
     * 5. Red-Line 4-Finger Wave Deployment & 5th Hero / Pet Surge
     */
    private fun executeRedLineDeployment() {
        if (!isRunning) return
        updateStatus("🔥 [RAIDING] Deploying 4-Finger Wave + Dragon Duke...")

        // Select Troop Slot 1 (Root Riders / Dragons)
        accessibilityService.performPercentageTap(0.105f, 0.900f) {
            handler.postDelayed({
                // 4-Finger Line Wave along South Red Line
                val start = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START
                val end = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END
                val lines = listOf(
                    Pair(start, end),
                    Pair(PointF(start.x, start.y - 0.015f), PointF(end.x, end.y - 0.015f))
                )

                accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 400L) {
                    handler.postDelayed({
                        // Select Troop Slot 2 (Valkyries / Loons)
                        accessibilityService.performPercentageTap(0.145f, 0.900f) {
                            accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 400L) {
                                // Deploy All Heroes (King, Queen, Warden, Champion)
                                deployAllHeroes {
                                    // Deploy 5th Hero (Dragon Duke) + Greedy Raven Pet
                                    dragonDuke.deployAndTrigger5thHero(PointF(0.500f, 0.785f)) {
                                        updateStatus("🛡️ [BATTLE] Core Charge (Warden Tome + Dragon Duke Active)...")
                                        handler.postDelayed({
                                            finishAttackAndReturnHome()
                                        }, 35000L)
                                    }
                                }
                            }
                        }
                    }, 500L)
                }
            }, 600L)
        }
    }

    private fun deployAllHeroes(onComplete: () -> Unit) {
        val heroSlots = listOf(
            UniversalFixedUiMapper.PCT_HERO_1_KING,
            UniversalFixedUiMapper.PCT_HERO_2_QUEEN,
            UniversalFixedUiMapper.PCT_HERO_3_WARDEN,
            UniversalFixedUiMapper.PCT_HERO_4_CHAMPION
        )
        var idx = 0
        fun deployNext() {
            if (idx < heroSlots.size && isRunning) {
                val heroSlot = heroSlots[idx++]
                accessibilityService.performPercentageTap(heroSlot) {
                    accessibilityService.performPercentageTap(0.500f, 0.785f) {
                        handler.postDelayed({ deployNext() }, 250L)
                    }
                }
            } else {
                onComplete()
            }
        }
        deployNext()
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
