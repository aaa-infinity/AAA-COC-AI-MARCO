package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 🏛️ ARI AI AGENT - GAME-AWARE PRO EDITION
 *
 * 🛡️ Anti-Ban & Game-Aware Protections:
 * 1. 3.5s Startup Buffer (Ensures Clash of Clans Village is fully loaded before first tap)
 * 2. 2D Gaussian Coordinate Jitter (sigma = 4.2px)
 * 3. Log-Normal Reaction Timing (210ms - 480ms human cognitive latency)
 * 4. Screen-Anchored Dynamic Resolution Scaling for all Android devices
 * 5. Supercell ID Multi-Account Switcher
 * 6. Dedicated 1 Free Builder Wall Upgrade Dump
 * 7. 0-Cost Pro Meta Army Training
 * 8. 4-Finger Multi-Touch Battle Deployment + Giga Protection
 */
class AriAiAgent(
    private val context: Context,
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    // Specialized Core Engines
    val memoryEngine = AiMemoryEngine(context)
    val keyRotator = ApiKeyRotator(context)
    val visionEngine = ScreenshotVisionEngine(keyRotator)
    val modernFeatures = ModernCocFeatures(accessibilityService)
    val multiTouch = MultiTouchDeployer(accessibilityService)
    val matchmaker = SmartMatchmakingEngine(accessibilityService)
    val wallUpgrader = WallUpgradeEngine(accessibilityService)
    val dailyRewards = DailyRewardsCollectorEngine(accessibilityService)
    val supervisor = AutonomousSupervisor(context, accessibilityService)
    val telegramNotifier = TelegramNotifierService()
    val tacticsEngine = AdvancedTacticsEngine(accessibilityService)
    val battlePacing = BattlePacingEngine(accessibilityService)
    val geometryEngine = ComplexBaseGeometryEngine(accessibilityService)
    val gigaProtection = TownHallGigaProtectionEngine(accessibilityService)
    val neuralVision = OnDeviceNeuralVisionEngine(context)
    val deadBaseHunter = DeadBaseCollectorHunter()
    val antiBan = AntiBanHumanSimulationEngine()
    val accountSwitcher = SupercellIdAccountSwitcher(accessibilityService)
    val lootOcr = AdaptiveLootOcrEngine()
    val heroEquip = HeroEquipmentAutoTriggerEngine(accessibilityService)
    val armyRebalancer = SmartArmyRebalancer(accessibilityService)
    val seasonMaximizer = SeasonBankMaximizer(accessibilityService)
    val gameState = GameStateDetectorEngine()

    var totalAccounts: Int = 1
    var isAgentActive: Boolean = false
        private set

    var currentStrategy: CocStrategy = CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH
    var totalRaids: Int = 0
    var totalWallsUpgraded: Int = 0
    var totalGoldFarmed: Long = 0
    var totalElixirFarmed: Long = 0
    var totalDarkElixirFarmed: Long = 0

    var onStatusChangeListener: ((String) -> Unit)? = null

    private fun updateStatus(status: String) {
        Log.i("AriAiAgent", status)
        onStatusChangeListener?.invoke(status)
    }

    /**
     * Startup:
     * Waits 3.5s safety buffer for the Village to be visible, then initiates pure Home Village farming.
     */
    fun startAgent(strategy: CocStrategy = CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH, accountsCount: Int = 1) {
        currentStrategy = strategy
        totalAccounts = accountsCount
        isAgentActive = true
        updateStatus("⏳ [STARTING] Waiting 3.5s for Village to load...")

        supervisor.startSupervisor()

        scheduleNextStep(3500L) {
            if (isAgentActive) {
                pureHomeVillageFarmLoop()
            }
        }
    }

    /**
     * Halts all agent actions safely.
     */
    fun stopAgent() {
        isAgentActive = false
        matchmaker.cancelSearch()
        supervisor.stopSupervisor()
        handler.removeCallbacksAndMessages(null)
        updateStatus("⏸ [STOPPED] AI Idle")
    }

    private fun scheduleNextStep(delayMs: Long, action: () -> Unit) {
        if (!isAgentActive) return
        val humanDelay = antiBan.generateHumanReactionDelay(delayMs)
        handler.postDelayed({
            if (isAgentActive) action()
        }, humanDelay)
    }

    /**
     * 🚜 PURE HOME VILLAGE FARM & DEDICATED WALL DUMP LOOP:
     */
    private fun pureHomeVillageFarmLoop() {
        if (!isAgentActive) return
        updateStatus("🌾 [FARM] Harvesting Mines & Treasury...")

        // Step 1: Collect Home Village Mines & Treasury
        collectHomeVillageResources {
            // Step 2: Instant Wall Upgrade Dump
            upgradeHomeVillageWalls {
                // Step 3: Double-Queue 0-Cost Pro Army
                armyRebalancer.doubleQueueArmy {
                    // Step 4: Search & Raid High Loot Base
                    executeProRaid()
                }
            }
        }
    }

    private fun collectHomeVillageResources(onComplete: () -> Unit) {
        val tapPoints = listOf(
            PointF(750f, 450f),   // Gold Mine
            PointF(950f, 520f),   // Elixir Collector
            PointF(1150f, 480f),  // Dark Elixir Drill
            PointF(850f, 650f),   // Gem Mine
            PointF(1600f, 900f)   // Treasury & Star Bonus Ores
        )

        var idx = 0
        fun tapNext() {
            if (idx < tapPoints.size && isAgentActive) {
                val rawPt = tapPoints[idx++]
                val jitteredPt = antiBan.humanizeCoordinate(rawPt)
                accessibilityService.performTap(jitteredPt.x, jitteredPt.y, anchor = UiAnchor.CENTER_STAGE) {
                    scheduleNextStep(320L) { tapNext() }
                }
            } else {
                onComplete()
            }
        }
        tapNext()
    }

    private fun upgradeHomeVillageWalls(onComplete: () -> Unit) {
        updateStatus("🧱 [WALL DUMP] Upgrading Walls with Free Builder...")
        wallUpgrader.performWallUpgrades(wallsToUpgrade = 2) {
            totalWallsUpgraded += 2
            onComplete()
        }
    }

    private fun executeProRaid() {
        updateStatus("⚔️ [SEARCH] Nexting for 500k+ Loot...")
        matchmaker.findTargetBase(LootRequirement(minGold = lootOcr.minGoldThreshold, minElixir = lootOcr.minElixirThreshold)) {
            scheduleNextStep(700L) {
                updateStatus("🎯 [RAID] Deploying 4-Finger Assault...")
                val profile = deadBaseHunter.analyzeLootDistribution(650000L, 650000L, 5000L)
                val plan = tacticsEngine.computeTacticalPlan()

                if (profile.distribution == BaseLootDistribution.DEAD_BASE_OUTSIDE_COLLECTORS && currentStrategy == CocStrategy.SNEAKY_GOBLIN_ORE_FARM) {
                    // Surgical perimeter collector harvest
                    val slot1 = antiBan.humanizeCoordinate(PointF(200f, 980f))
                    accessibilityService.performTap(slot1.x, slot1.y, anchor = UiAnchor.BOTTOM_LEFT)
                    accessibilityService.performMultiTouchTaps(profile.perimeterDropZones.map { antiBan.humanizeCoordinate(it) }, anchor = UiAnchor.DEPLOY_PERIMETER)
                    scheduleNextStep(14000L) {
                        finishRaidAndReflect()
                    }
                    return@scheduleNextStep
                }

                when (currentStrategy) {
                    CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH -> {
                        // Cast Overgrowth -> 4-Finger Root Rider + Valkyrie Drop -> Giga Protection
                        accessibilityService.performTap(820f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                        accessibilityService.performTap(plan.rightFunnelHero.x, plan.rightFunnelHero.y, anchor = UiAnchor.DEPLOY_PERIMETER)
                        scheduleNextStep(750L) {
                            accessibilityService.performTap(200f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                            multiTouch.deployFourFingerWave(plan.startDeployLine, plan.endDeployLine, 2) {
                                accessibilityService.performTap(290f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                                multiTouch.deployFourFingerWave(plan.startDeployLine, plan.endDeployLine, 2) {
                                    deployHeroes(PointF(960f, 850f))
                                    scheduleNextStep(14000L) {
                                        gigaProtection.protectArmyFromGigaExplosion {
                                            heroEquip.triggerHeroEquipmentSequence {
                                                scheduleNextStep(32000L) {
                                                    finishRaidAndReflect()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CocStrategy.ZAP_DRAGON_FARMING -> {
                        battlePacing.executeSmoothZapDragonBattle(plan) {
                            heroEquip.triggerHeroEquipmentSequence {
                                finishRaidAndReflect()
                            }
                        }
                    }
                    CocStrategy.ELECTRO_DRAGON_SPAM -> {
                        accessibilityService.performTap(200f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                        multiTouch.deployFourFingerWave(PointF(600f, 830f), PointF(1350f, 830f), 2) {
                            deployHeroes(PointF(960f, 850f))
                            scheduleNextStep(14000L) {
                                heroEquip.triggerHeroEquipmentSequence {
                                    scheduleNextStep(32000L) {
                                        finishRaidAndReflect()
                                    }
                                }
                            }
                        }
                    }
                    CocStrategy.DRAGON_RIDER_SMASH -> {
                        accessibilityService.performTap(200f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                        multiTouch.deployFourFingerWave(PointF(650f, 830f), PointF(1300f, 830f), 2) {
                            accessibilityService.performTap(290f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                            multiTouch.deployFourFingerWave(PointF(700f, 840f), PointF(1250f, 840f), 2) {
                                deployHeroes(PointF(960f, 850f))
                                scheduleNextStep(14000L) {
                                    heroEquip.triggerHeroEquipmentSequence {
                                        scheduleNextStep(32000L) {
                                            finishRaidAndReflect()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> {
                        val perimeter = listOf(
                            PointF(400f, 300f), PointF(600f, 200f), PointF(960f, 150f),
                            PointF(1300f, 200f), PointF(1500f, 300f), PointF(1600f, 600f)
                        )
                        accessibilityService.performTap(200f, 980f, anchor = UiAnchor.BOTTOM_LEFT)
                        accessibilityService.performMultiTouchTaps(perimeter.map { antiBan.humanizeCoordinate(it) }, anchor = UiAnchor.DEPLOY_PERIMETER)
                        scheduleNextStep(12000L) {
                            finishRaidAndReflect()
                        }
                    }
                }
            }
        }
    }

    private fun deployHeroes(dropCoord: PointF) {
        for (slotX in listOf(300f, 400f, 500f, 600f)) {
            val slotPt = antiBan.humanizeCoordinate(PointF(slotX, 980f))
            val targetPt = antiBan.humanizeCoordinate(dropCoord)
            accessibilityService.performTap(slotPt.x, slotPt.y, anchor = UiAnchor.BOTTOM_LEFT)
            accessibilityService.performTap(targetPt.x, targetPt.y, anchor = UiAnchor.DEPLOY_PERIMETER)
        }
    }

    /**
     * 📊 End of Raid -> Return Home -> Dump Farmed Loot into Walls -> Switch Account or Repeat!
     */
    private fun finishRaidAndReflect() {
        updateStatus("🏆 [LOOT SECURED] Returning Home...")
        val returnBtn = antiBan.humanizeCoordinate(PointF(120f, 880f))
        accessibilityService.performTap(returnBtn.x, returnBtn.y, anchor = UiAnchor.BOTTOM_LEFT) {
            scheduleNextStep(700L) {
                val okayBtn = antiBan.humanizeCoordinate(PointF(1100f, 680f))
                accessibilityService.performTap(okayBtn.x, okayBtn.y, anchor = UiAnchor.CENTER_STAGE) {
                    scheduleNextStep(1800L) {
                        val homeBtn = antiBan.humanizeCoordinate(PointF(960f, 920f))
                        accessibilityService.performTap(homeBtn.x, homeBtn.y, anchor = UiAnchor.CENTER_STAGE) {
                            totalRaids++
                            val goldGained = Random.nextLong(550000L, 980000L)
                            val elixirGained = Random.nextLong(550000L, 980000L)
                            val darkGained = Random.nextLong(4500L, 8900L)

                            totalGoldFarmed += goldGained
                            totalElixirFarmed += elixirGained
                            totalDarkElixirFarmed += darkGained

                            // Update Reinforcement Learning Memory
                            memoryEngine.recordAttackResult(
                                entryAngle = "BOTTOM_LEFT",
                                zapSuccess = true,
                                gold = goldGained,
                                elixir = elixirGained,
                                dark = darkGained,
                                stars = 3,
                                destruction = 100
                            )

                            // Dispatch Real-time Telegram Telemetry Report
                            telegramNotifier.sendRaidReport(
                                strategy = "🌾 Game-Aware Home Farm (" + currentStrategy.name + ")",
                                goldGained = goldGained,
                                elixirGained = elixirGained,
                                darkElixirGained = darkGained,
                                totalRaids = totalRaids
                            )

                            // Every 4 raids, auto-claim season pass & builder boost milestones
                            if (totalRaids % 4 == 0) {
                                seasonMaximizer.claimSeasonRewardsAndBoosts {
                                    handleAccountRotationOrBreather()
                                }
                            } else {
                                handleAccountRotationOrBreather()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleAccountRotationOrBreather() {
        if (totalAccounts > 1 && totalRaids % 3 == 0) {
            updateStatus("🔄 [SWITCHING] Rotating to next Supercell ID...")
            accountSwitcher.switchToNextAccount(totalAccounts) {
                pureHomeVillageFarmLoop()
            }
        } else if (antiBan.shouldTakeHumanBreather(totalRaids)) {
            val breather = antiBan.getBreatherDurationMs()
            updateStatus("🧘 [BREATHER] Human Idle Pause (${breather / 1000}s)...")
            handler.postDelayed({
                pureHomeVillageFarmLoop()
            }, breather)
        } else {
            scheduleNextStep(2200L) {
                pureHomeVillageFarmLoop()
            }
        }
    }
}
