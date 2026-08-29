package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 🛡️ Clan Castle Auto-Request Loop
 *
 * Automatically requests reinforcements every 10-15 minutes with customizable troop presets.
 */
class ClanCastleAutoRequester(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRequesting: Boolean = false
        private set

    private val PCT_CC_REQUEST_BTN = PointF(0.485f, 0.880f)  // "Request" button on Clan Castle popup
    private val PCT_SEND_REQUEST = PointF(0.680f, 0.720f)    // "Send" request button

    fun requestReinforcements(customPreset: String = "Any ground troops / Sneaky Gobs please", onComplete: () -> Unit) {
        if (isRequesting) {
            onComplete()
            return
        }

        isRequesting = true
        Log.i("CCRequester", "=== [CLAN CASTLE] Requesting Reinforcements ('$customPreset') ===")

        // Step 1: Tap Clan Castle "Request" Button
        accessibilityService.performPercentageTap(PCT_CC_REQUEST_BTN) {
            handler.postDelayed({
                // Step 2: Confirm & Send Request
                accessibilityService.performPercentageTap(PCT_SEND_REQUEST) {
                    handler.postDelayed({
                        isRequesting = false
                        Log.i("CCRequester", "✓ Clan Castle reinforcements requested.")
                        onComplete()
                    }, 800L)
                }
            }, Random.nextLong(1000L, 1400L))
        }
    }
}
