package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * 🌊 Wave-Based Funnel & Combat Orchestrator
 *
 * Wave 1 (Funnel): Outer collector cleanup (Sneaky Goblins / Baby Dragons)
 * Wave 2 (Core Push): Main 4-finger swarm deployment along optimal flank
 * Wave 3 (Hero & Equipment): Sequenced hero equipment triggers (Warden Tome, Gauntlet, Invisibility)
 */
class TacticalWaveOrchestrator(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isOrchestrating: Boolean = false
        private set

    fun execute3PhaseWaveAttack(
        startLinePct: PointF,
        endLinePct: PointF,
        wardenDelaySec: Int = 12,
        onComplete: () -> Unit
    ) {
        if (isOrchestrating) return
        isOrchestrating = true
        Log.i("WaveOrchestrator", "=== [WAVE ATTACK] Initiating 3-Phase Funnel Orchestration ===")

        // ==================== WAVE 1: FUNNEL & OUTER PERIMETER ====================
        Log.i("WaveOrchestrator", "🌊 Wave 1: Deploying outer flank funnel troops...")
        val funnelLeft = PointF(startLinePct.x - 0.060f, startLinePct.y)
        val funnelRight = PointF(endLinePct.x + 0.060f, endLinePct.y)

        // Select Funnel Unit (Slot 1)
        accessibilityService.performPercentageTap(0.105f, 0.900f) {
            accessibilityService.performPercentageTap(funnelLeft) {
                accessibilityService.performPercentageTap(funnelRight) {
                    handler.postDelayed({
                        // ==================== WAVE 2: MAIN CORE PUSH ====================
                        Log.i("WaveOrchestrator", "🌊 Wave 2: Deploying main heavy force (4-Finger Bezier Sweep)...")
                        val line1 = Pair(startLinePct, endLinePct)
                        val line2 = Pair(PointF(startLinePct.x, startLinePct.y - 0.015f), PointF(endLinePct.x, endLinePct.y - 0.015f))

                        // Select Main Force (Slot 2)
                        accessibilityService.performPercentageTap(0.145f, 0.900f) {
                            accessibilityService.performPercentageMultiFingerSwipes(listOf(line1, line2), durationMs = 420L) {
                                handler.postDelayed({
                                    // Deploy Heroes (Slots 3-6)
                                    deployAllHeroesAndSpells(startLinePct, endLinePct) {
                                        // ==================== WAVE 3: HERO EQUIPMENT SURGE ====================
                                        Log.i("WaveOrchestrator", "🌊 Wave 3: Scheduling Warden Eternal Tome in ${wardenDelaySec}s...")
                                        handler.postDelayed({
                                            triggerHeroEquipmentSurge {
                                                isOrchestrating = false
                                                onComplete()
                                            }
                                        }, wardenDelaySec * 1000L)
                                    }
                                }, 800L)
                            }
                        }
                    }, 1800L)
                }
            }
        }
    }

    private fun deployAllHeroesAndSpells(startLine: PointF, endLine: PointF, onHeroesDropped: () -> Unit) {
        val heroSlots = listOf(
            UniversalFixedUiMapper.PCT_HERO_1_KING,
            UniversalFixedUiMapper.PCT_HERO_2_QUEEN,
            UniversalFixedUiMapper.PCT_HERO_3_WARDEN,
            UniversalFixedUiMapper.PCT_HERO_4_CHAMPION
        )
        val centerDrop = PointF((startLine.x + endLine.x) / 2f, (startLine.y + endLine.y) / 2f)

        var idx = 0
        fun dropNextHero() {
            if (idx < heroSlots.size) {
                val slot = heroSlots[idx++]
                accessibilityService.performPercentageTap(slot) {
                    accessibilityService.performPercentageTap(centerDrop) {
                        handler.postDelayed({ dropNextHero() }, 280L)
                    }
                }
            } else {
                onHeroesDropped()
            }
        }
        dropNextHero()
    }

    private fun triggerHeroEquipmentSurge(onFinished: () -> Unit) {
        Log.i("WaveOrchestrator", "⚡ [HERO SURGE] Activating Grand Warden Eternal Tome + Giant Gauntlet!")

        // Tap Warden (Invincibility Bubble)
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_3_WARDEN) {
            handler.postDelayed({
                // Tap King (Giant Gauntlet Earthquakes)
                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_1_KING) {
                    handler.postDelayed({
                        // Tap Queen (Invisibility Cloak)
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_2_QUEEN) {
                            handler.postDelayed({
                                // Tap Champion (Seeking Shield / Rocket Spear)
                                accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_4_CHAMPION) {
                                    Log.i("WaveOrchestrator", "✓ All active hero equipment engaged.")
                                    onFinished()
                                }
                            }, 500L)
                        }
                    }, 500L)
                }
            }, 500L)
        }
    }
}
