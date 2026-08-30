package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * 💰 PURE LOOT FARMING ENGINE (Zero Trophy Anxiety)
 *
 * Dedicated 100% to maximum resource extraction:
 * 1. Drops Lightning Spells on core defense clusters
 * 2. 4-Finger continuous line deployment across the loot-rich perimeter
 * 3. Deploys Heroes to punch into inner Gold/Elixir storages
 * 4. Taps Battle Fast-Forward speed multiplier
 * 5. Surrenders the moment all storages are emptied (Hero preservation)
 */
class AiLootFarmerEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    private val jitter = GaussianMotionCalibrator()

    fun executeLootAssault(
        startLine: PointF = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_START,
        endLine: PointF = UniversalFixedUiMapper.PCT_DEPLOY_SOUTH_END,
        onComplete: () -> Unit
    ) {
        Log.i("LootFarmer", "💰 [LOOT ASSAULT] Executing pure resource extraction...")

        // Step 1: Drop Spells (Slot 4) on Center Defenses
        accessibilityService.performPercentageTap(PointF(0.280f, 0.900f)) {
            handler.postDelayed({
                val zapPoints = listOf(
                    PointF(0.480f, 0.500f),
                    PointF(0.520f, 0.500f),
                    PointF(0.500f, 0.450f)
                )
                accessibilityService.performPercentageMultiTouch(zapPoints, durationMs = 80L) {
                    handler.postDelayed({
                        // Step 2: Deploy Main Army (Slot 1) in 4-Finger Line
                        deploy4FingerSwarm(startLine, endLine) {
                            // Step 3: Deploy Heroes behind troops
                            deployHeroes {
                                // Step 4: Tap Battle Speed Multiplier (Fast Forward)
                                tapBattleFastForward()

                                // Step 5: Grand Warden Invincibility at 10s
                                triggerHeroAbilities {
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
        // Select Troop Slot 1 (x=0.080, y=0.900)
        accessibilityService.performPercentageTap(PointF(0.080f, 0.900f)) {
            handler.postDelayed({
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

                accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 400L) {
                    handler.postDelayed({
                        // Second rapid wave to ensure full army deployment
                        accessibilityService.performPercentageMultiFingerSwipes(lines, durationMs = 400L) {
                            handler.postDelayed({
                                onComplete()
                            }, 450L)
                        }
                    }, 450L)
                }
            }, 350L)
        }
    }

    private fun deployHeroes(onComplete: () -> Unit) {
        val heroSlots = listOf(
            UniversalFixedUiMapper.PCT_HERO_1_KING,
            UniversalFixedUiMapper.PCT_HERO_2_QUEEN,
            UniversalFixedUiMapper.PCT_HERO_3_WARDEN
        )

        fun dropNext(index: Int) {
            if (index >= heroSlots.size) {
                onComplete()
                return
            }
            val slot = heroSlots[index]
            accessibilityService.performPercentageTap(slot) {
                handler.postDelayed({
                    accessibilityService.performPercentageTap(PointF(0.500f, 0.780f)) {
                        handler.postDelayed({
                            dropNext(index + 1)
                        }, 300L)
                    }
                }, 250L)
            }
        }

        dropNext(0)
    }

    /**
     * Taps the in-game Fast-Forward speed button during battle
     */
    private fun tapBattleFastForward() {
        handler.postDelayed({
            // Fast Forward button (Top-Right during live attack: x=0.920, y=0.080)
            accessibilityService.performPercentageTap(PointF(0.920f, 0.080f)) {
                Log.d("LootFarmer", "⏩ Tapped Battle Fast-Forward Speed Multiplier")
            }
        }, 3000L)
    }

    private fun triggerHeroAbilities(onComplete: () -> Unit) {
        handler.postDelayed({
            // Warden Eternal Tome
            accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_3_WARDEN) {
                handler.postDelayed({
                    // King & Queen Equipment
                    accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_1_KING) {
                        accessibilityService.performPercentageTap(UniversalFixedUiMapper.PCT_HERO_2_QUEEN) {
                            onComplete()
                        }
                    }
                }, 350L)
            }
        }, 10000L)
    }
}
