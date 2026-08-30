package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * 💰 Real Pure Loot Extraction Engine
 * Deploys troops and spells dynamically on perimeter collectors and inner storages.
 */
class AiLootFarmerEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    fun executeLootAssault(
        startLine: PointF = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START,
        endLine: PointF = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END,
        onComplete: () -> Unit
    ) {
        Log.i("LootFarmer", "💰 [REAL AI LOOT ASSAULT] Extracting Gold, Elixir & Dark Elixir...")

        // 1. Drop Spells (Slot 1 / Lightning) on Center Defense cluster
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_SPELL_SLOT_1) {
            handler.postDelayed({
                val zapTargets = listOf(
                    PointF(0.480f, 0.500f),
                    PointF(0.520f, 0.500f),
                    PointF(0.500f, 0.460f)
                )
                accessibilityService.performPercentageMultiTouch(zapTargets, durationMs = 80L) {
                    handler.postDelayed({
                        // 2. Deploy Troop Wave (Slot 1) in 4-Finger Line
                        deploy4FingerSwarm(startLine, endLine) {
                            // 3. Deploy Heroes behind the wave
                            deployHeroes {
                                // 4. Tap Battle Speed Multiplier (Fast Forward)
                                tapFastForward()

                                // 5. Grand Warden Invincibility & Hero Equipment at 10s
                                triggerHeroEquipment {
                                    onComplete()
                                }
                            }
                        }
                    }, 400L)
                }
            }, 350L)
        }
    }

    private fun deploy4FingerSwarm(startLine: PointF, endLine: PointF, onComplete: () -> Unit) {
        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_TROOP_SLOT_1) {
            handler.postDelayed({
                val lines = mutableListOf<Pair<PointF, PointF>>()
                val steps = 4
                for (i in 0 until steps) {
                    val t1 = i.toFloat() / steps.toFloat()
                    val t2 = (i + 1).toFloat() / steps.toFloat()
                    val p1 = PointF(startLine.x + t1 * (endLine.x - startLine.x), startLine.y + t1 * (endLine.y - startLine.y))
                    val p2 = PointF(startLine.x + t2 * (endLine.x - startLine.x), startLine.y + t2 * (endLine.y - startLine.y))
                    lines.add(Pair(p1, p2))
                }

                accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 380L) {
                    handler.postDelayed({
                        // Second deployment wave to empty army tray
                        accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 380L) {
                            handler.postDelayed({ onComplete() }, 400L)
                        }
                    }, 400L)
                }
            }, 300L)
        }
    }

    private fun deployHeroes(onComplete: () -> Unit) {
        val heroes = listOf(
            UniversalFixedUiMapper.PCT_HERO_1_KING,
            UniversalFixedUiMapper.PCT_HERO_2_QUEEN,
            UniversalFixedUiMapper.PCT_HERO_3_WARDEN
        )

        fun drop(idx: Int) {
            if (idx >= heroes.size) {
                onComplete()
                return
            }
            accessibilityService.performPercentageTap(heroes[idx]) {
                handler.postDelayed({
                    accessibilityService.performPercentageTap(PointF(0.500f, 0.780f)) {
                        handler.postDelayed({ drop(idx + 1) }, 250L)
                    }
                }, 200L)
            }
        }
        drop(0)
    }

    private fun tapFastForward() {
        handler.postDelayed({
            accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_BATTLE_FAST_FORWARD) {
                Log.d("LootFarmer", "⏩ Tapped Battle Fast-Forward Speed Multiplier")
            }
        }, 2500L)
    }

    private fun triggerHeroEquipment(onComplete: () -> Unit) {
        handler.postDelayed({
            accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_3_WARDEN) {
                handler.postDelayed({
                    accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_1_KING) {
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_2_QUEEN) {
                            onComplete()
                        }
                    }
                }, 300L)
            }
        }, 10000L)
    }
}
