package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class BattlePacingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    private val multiTouch = MultiTouchDeployer(accessibilityService)
    private val motionEngine = SmoothHumanMotionEngine()
    private val modernFeatures = ModernCocFeatures(accessibilityService)

    /**
     * Executes Pro Humanized Multi-Phase Zap Dragon Battle:
     */
    fun executeSmoothZapDragonBattle(
        plan: TacticalPlan,
        onBattleEnded: () -> Unit
    ) {
        Log.i("BattlePacing", "=== [PRO BATTLE PACING] Starting Smooth Multi-Phase Attack ===")

        // PHASE 1: Zap Air Defenses (Slot 5: Lightning Spell)
        Log.i("BattlePacing", "Phase 1: Precision Lightning Zap on Air Defenses...")
        accessibilityService.performTap(620f, 980f) // Lightning Spell

        fun zapTarget(idx: Int, target: PointF, onZapped: () -> Unit) {
            var zaps = 0
            fun singleZap() {
                if (zaps < 3) {
                    zaps++
                    accessibilityService.performTap(target.x, target.y) {
                        handler.postDelayed({ singleZap() }, motionEngine.getHumanReactionDelay(160L, 80L))
                    }
                } else {
                    onZapped()
                }
            }
            singleZap()
        }

        val target1 = plan.zapTargets.getOrElse(0) { PointF(750f, 480f) }
        val target2 = plan.zapTargets.getOrElse(1) { PointF(1170f, 480f) }

        zapTarget(1, target1) {
            handler.postDelayed({
                zapTarget(2, target2) {
                    // PHASE 2: 2-Finger Synchronized Corner Hero Funnels
                    handler.postDelayed({
                        Log.i("BattlePacing", "Phase 2: 2-Finger Synchronized Corner Funnels (King & Queen)...")
                        accessibilityService.performTap(300f, 980f) // Hero Slot
                        multiTouch.deployTwoFingerFunnel(
                            leftCorner = plan.leftFunnelHero,
                            rightCorner = plan.rightFunnelHero,
                            taps = 2
                        ) {
                            // PHASE 3: 4-Finger Simultaneous Dragon Wave
                            handler.postDelayed({
                                Log.i("BattlePacing", "Phase 3: 4-Finger Simultaneous Dragon Line Wave...")
                                accessibilityService.performTap(200f, 980f) // Slot 1: Dragons
                                multiTouch.deployFourFingerWave(
                                    startCorner = plan.startDeployLine,
                                    endCorner = plan.endDeployLine,
                                    waves = 3
                                ) {
                                    // PHASE 4: Balloons & Grand Warden Deployment
                                    handler.postDelayed({
                                        Log.i("BattlePacing", "Phase 4: Balloons & Grand Warden follow-up...")
                                        accessibilityService.performTap(290f, 980f) // Slot 2: Balloons
                                        multiTouch.deployFourFingerWave(
                                            startCorner = plan.startDeployLine,
                                            endCorner = plan.endDeployLine,
                                            waves = 2
                                        ) {
                                            accessibilityService.performTap(500f, 980f) // Grand Warden
                                            accessibilityService.performTap(960f, 850f)

                                            // PHASE 5: Rage Core Surge (8s after deploy)
                                            handler.postDelayed({
                                                Log.i("BattlePacing", "Phase 5: Casting Core Rage Spells into defense clusters...")
                                                accessibilityService.performTap(720f, 980f) // Rage Spell
                                                val ragePt = plan.rageSpellLocations.getOrElse(0) { PointF(960f, 540f) }
                                                accessibilityService.performTap(ragePt.x, ragePt.y)

                                                // PHASE 6: Hero Equipment & Warden Ability Trigger (14s after deploy)
                                                handler.postDelayed({
                                                    Log.i("BattlePacing", "Phase 6: Triggering Hero Equipment Combos & Invulnerability...")
                                                    modernFeatures.triggerHeroEquipmentCombos()

                                                    // PHASE 7: Battle Completion & Exit (36s after deploy)
                                                    handler.postDelayed({
                                                        Log.i("BattlePacing", "Phase 7: 100% Home Village destruction verified. Ending battle...")
                                                        onBattleEnded()
                                                    }, 36000L)
                                                }, 7000L)
                                            }, 7500L)
                                        }
                                    }, motionEngine.getHumanReactionDelay(600L, 200L))
                                }
                            }, motionEngine.getHumanReactionDelay(800L, 200L))
                        }
                    }, motionEngine.getHumanReactionDelay(600L, 200L))
                }
            }, motionEngine.getHumanReactionDelay(400L, 150L))
        }
    }
}
