package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * 🐉 PURE DRAGON FARMING ENGINE (Zap Dragons & Mass Air Assault)
 *
 * Dedicated 100% to fast, zero-cost Dragon army loot farming:
 * 1. Step 1: Cast Lightning Spells on primary Air Defenses
 * 2. Step 2: 4-Finger continuous line deployment of Dragons along loot edge
 * 3. Step 3: Hero Deployment (Barbarian King, Archer Queen, Grand Warden) behind Dragons
 * 4. Step 4: Grand Warden Eternal Tome + Hero Equipment activation at 10-12s
 * 5. Step 5: Extraction delay (25-30s) -> Surrender & Return Home with full loot!
 */
class DragonFarmingEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    private val jitterCalibrator = GaussianMotionCalibrator()

    /**
     * Executes the full Zap Dragon farming deployment
     */
    fun executeDragonAssault(
        startLine: PointF = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START,
        endLine: PointF = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END,
        onComplete: () -> Unit
    ) {
        Log.i("DragonEngine", "🐉 [ZAP DRAGONS] Initiating Mass Dragon Loot Assault...")

        // Step 1: Select Lightning Spells (Slot 4) and Zap Air Defenses
        castLightningSpells {
            // Step 2: Select Dragons (Slot 1) & 4-Finger Line Deployment
            deployMassDragons(startLine, endLine) {
                // Step 3: Deploy Heroes behind Dragons
                deployHeroesBehindDragons {
                    // Step 4: Grand Warden Invincibility + Hero Equipment Trigger
                    triggerWardenAndHeroAbilities {
                        Log.i("DragonEngine", "🐉 [DRAGON ASSAULT] Mass Dragons clearing base...")
                        onComplete()
                    }
                }
            }
        }
    }

    /**
     * 1. Cast Lightning Spells on Air Defenses
     */
    private fun castLightningSpells(onComplete: () -> Unit) {
        // Tap Lightning Spell Slot in Battle Bar (Slot 4: x=0.280, y=0.900)
        accessibilityService.performPercentageTap(PointF(0.280f, 0.900f)) {
            handler.postDelayed({
                // Drop 3-4 Lightning Spells near center defense clusters
                val zapPoints = listOf(
                    PointF(0.450f, 0.480f),
                    PointF(0.550f, 0.480f),
                    PointF(0.500f, 0.520f)
                )
                accessibilityService.performPercentageMultiTouch(zapPoints, durationMs = 85L) {
                    handler.postDelayed({
                        onComplete()
                    }, jitterCalibrator.getHumanizedDelayMs(600L))
                }
            }, 400L)
        }
    }

    /**
     * 2. Select Dragons & Multi-Finger Line Deployment
     */
    private fun deployMassDragons(startLine: PointF, endLine: PointF, onComplete: () -> Unit) {
        // Tap Dragon Slot (Slot 1: x=0.080, y=0.900)
        accessibilityService.performPercentageTap(PointF(0.080f, 0.900f)) {
            handler.postDelayed({
                // Create 4 parallel finger deployment swipes along the perimeter
                val lines = mutableListOf<Pair<PointF, PointF>>()
                val steps = 4
                for (i in 0 until steps) {
                    val t1 = i.toFloat() / steps.toFloat()
                    val t2 = (i + 1).toFloat() / steps.toFloat()
                    val p1 = PointF(
                        startLine.x + t1 * (endLine.x - startLine.x),
                        startLine.y + t1 * (endLine.y - startLine.y)
                    )
                    val p2 = PointF(
                        startLine.x + t2 * (endLine.x - startLine.x),
                        startLine.y + t2 * (endLine.y - startLine.y)
                    )
                    lines.add(Pair(p1, p2))
                }

                accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 450L) {
                    handler.postDelayed({
                        // Second wave of dragons to ensure entire army is deployed
                        accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 450L) {
                            handler.postDelayed({
                                onComplete()
                            }, 500L)
                        }
                    }, 500L)
                }
            }, 400L)
        }
    }

    /**
     * 3. Deploy Heroes behind the Dragons
     */
    private fun deployHeroesBehindDragons(onComplete: () -> Unit) {
        val heroSlots = listOf(
            UniversalFixedUiMapper.PCT_HERO_1_KING,
            UniversalFixedUiMapper.PCT_HERO_2_QUEEN,
            UniversalFixedUiMapper.PCT_HERO_3_WARDEN
        )

        fun dropNextHero(index: Int) {
            if (index >= heroSlots.size) {
                onComplete()
                return
            }

            val slot = heroSlots[index]
            accessibilityService.performPercentageTap(slot) {
                handler.postDelayed({
                    // Tap center deployment edge (x=0.500, y=0.785)
                    accessibilityService.performPercentageTap(PointF(0.500f, 0.785f)) {
                        handler.postDelayed({
                            dropNextHero(index + 1)
                        }, 350L)
                    }
                }, 300L)
            }
        }

        dropNextHero(0)
    }

    /**
     * 4. Trigger Grand Warden Eternal Tome + Hero Equipment after 10s
     */
    private fun triggerWardenAndHeroAbilities(onComplete: () -> Unit) {
        handler.postDelayed({
            // Tap Grand Warden Ability (Invincibility Tome protects all Dragons from Air Mines)
            accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_3_WARDEN) {
                handler.postDelayed({
                    // Tap Barbarian King (Giant Gauntlet) & Archer Queen (Invisibility)
                    accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_1_KING) {
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_2_QUEEN) {
                            onComplete()
                        }
                    }
                }, 400L)
            }
        }, 10000L) // 10 seconds into the raid
    }
}
