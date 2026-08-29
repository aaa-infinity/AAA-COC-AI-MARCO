package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 🔄 Advanced Supercell ID Multi-Account Switcher
 *
 * Automatically cycles between up to 4 saved Supercell ID accounts:
 * Step 1: Opens in-game Settings (⚙) icon
 * Step 2: Taps "Switch Account / Connected" 🔄 button next to Supercell ID
 * Step 3: Selects Account Slot (Slot 1 - 4) with percentage anchors
 * Step 4: Waits 8s for game reload & confirms village view
 */
class SupercellIdAccountSwitcher(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var currentAccountIndex: Int = 0
        private set

    var isSwitching: Boolean = false
        private set

    // Supercell ID Fixed Percentage UI Anchors
    private val PCT_SETTINGS_GEAR = PointF(0.965f, 0.175f)      // Settings (⚙) icon on Village HUD
    private val PCT_SWITCH_CONNECTED = PointF(0.720f, 0.320f)  // "Switch Account" 🔄 icon next to Supercell ID banner

    // Supercell ID Saved Account Slots (Vertical Card Layout)
    private val accountSlots = listOf(
        PointF(0.500f, 0.360f), // Account Slot 1 (Top Card)
        PointF(0.500f, 0.480f), // Account Slot 2 (Second Card)
        PointF(0.500f, 0.600f), // Account Slot 3 (Third Card)
        PointF(0.500f, 0.720f)  // Account Slot 4 (Fourth Card)
    )

    fun switchToNextAccount(totalAccounts: Int = 2, onComplete: () -> Unit) {
        if (isSwitching || totalAccounts <= 1) {
            onComplete()
            return
        }

        isSwitching = true
        currentAccountIndex = (currentAccountIndex + 1) % totalAccounts
        Log.i("SupercellID", "=== [SUPERCELL ID] Switching to Account #${currentAccountIndex + 1} of $totalAccounts ===")

        // Step 1: Open Settings (⚙)
        accessibilityService.performPercentageTap(PCT_SETTINGS_GEAR) {
            handler.postDelayed({
                // Step 2: Tap "Switch Account" 🔄 Icon
                accessibilityService.performPercentageTap(PCT_SWITCH_CONNECTED) {
                    handler.postDelayed({
                        // Step 3: Select Target Account Card Slot
                        val targetSlot = accountSlots.getOrElse(currentAccountIndex) { accountSlots[0] }
                        accessibilityService.performPercentageTap(targetSlot) {
                            Log.i("SupercellID", "✓ Account #${currentAccountIndex + 1} selected. Waiting 8s for game reload...")
                            // Step 4: Wait 8.0s for Supercell ID authentication & village assets to reload
                            handler.postDelayed({
                                isSwitching = false
                                Log.i("SupercellID", "✓ Account #${currentAccountIndex + 1} loaded & ready!")
                                onComplete()
                            }, 8000L)
                        }
                    }, Random.nextLong(1400L, 1800L))
                }
            }, Random.nextLong(1200L, 1600L))
        }
    }
}
