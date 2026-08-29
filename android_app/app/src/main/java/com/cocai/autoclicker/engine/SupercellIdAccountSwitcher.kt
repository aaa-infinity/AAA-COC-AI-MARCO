package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class SupercellIdAccountSwitcher(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isSwitchingAccount: Boolean = false
        private set

    var currentAccountIndex: Int = 0

    // Coordinates for account list in Supercell ID Switcher modal
    private val accountSlots = listOf(
        PointF(960f, 420f),  // Account 1
        PointF(960f, 540f),  // Account 2
        PointF(960f, 660f),  // Account 3
        PointF(960f, 780f)   // Account 4
    )

    /**
     * Autonomous Supercell ID Multi-Account Switcher:
     * 1. Opens In-Game Settings (bottom-right gear x=1850, y=720)
     * 2. Taps Supercell ID Switch Account button (x=1600, y=380)
     * 3. Selects next saved Supercell ID Account
     * 4. Waits for village load & resumes farming
     */
    fun switchToNextAccount(totalAccounts: Int = 2, onComplete: () -> Unit) {
        if (isSwitchingAccount || totalAccounts <= 1) {
            onComplete()
            return
        }
        isSwitchingAccount = true
        currentAccountIndex = (currentAccountIndex + 1) % totalAccounts
        Log.i("SupercellID", "=== [SUPERCELL ID SWITCHER] Switching to Account #${currentAccountIndex + 1} of $totalAccounts ===")

        // Step 1: Open Settings (gear icon at x=1850, y=720)
        accessibilityService.performTap(1850f, 720f) {
            handler.postDelayed({
                // Step 2: Tap Supercell ID Switch Account button (x=1600, y=380)
                accessibilityService.performTap(1600f, 380f) {
                    handler.postDelayed({
                        // Step 3: Tap Account Slot in list
                        val slot = accountSlots.getOrElse(currentAccountIndex) { accountSlots[0] }
                        Log.i("SupercellID", "Selecting Supercell ID slot at (${slot.x}, ${slot.y})...")
                        accessibilityService.performTap(slot.x, slot.y) {
                            // Step 4: Allow 6-8 seconds for game reload & village transition
                            handler.postDelayed({
                                isSwitchingAccount = false
                                Log.i("SupercellID", "✓ Switched to Account #${currentAccountIndex + 1}. Resuming farming loop.")
                                onComplete()
                            }, Random.nextLong(6500L, 8500L))
                        }
                    }, Random.nextLong(1200L, 1800L))
                }
            }, Random.nextLong(1000L, 1500L))
        }
    }
}
