package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService

/**
 * ❌ Startup Popup & Defense Log Dismissal Engine
 *
 * Automatically clears all modern Clash of Clans startup dialogs:
 * 1. "Your village was raided!" defense summary popups ("Okay" button)
 * 2. Season Pass / Event News popups (Top-right "X" button)
 * 3. Special Shop Offer / Promo modals
 * 4. Clan War / CWL result dialogs
 */
class StartupPopupDismissEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())

    fun dismissAllStartupPopups(onComplete: () -> Unit) {
        Log.i("PopupDismiss", "🧹 Clearing startup popups, news, and defense logs...")

        // 1. Tap Center-Bottom "Okay" (Defense raid summary popup: x=0.500, y=0.820)
        accessibilityService.performPercentageTap(PointF(0.500f, 0.820f)) {
            handler.postDelayed({
                // 2. Tap Top-Right "X" (Season / Event news modals: x=0.925, y=0.120)
                accessibilityService.performPercentageTap(PointF(0.925f, 0.120f)) {
                    handler.postDelayed({
                        // 3. Tap Center-Right "X" (Special offer popups: x=0.880, y=0.180)
                        accessibilityService.performPercentageTap(PointF(0.880f, 0.180f)) {
                            handler.postDelayed({
                                // 4. Tap Bottom-Center "Okay" confirm for any remaining modal
                                accessibilityService.performPercentageTap(PointF(0.500f, 0.750f)) {
                                    handler.postDelayed({
                                        Log.i("PopupDismiss", "✓ Screen cleared of all startup dialogs")
                                        onComplete()
                                    }, 400L)
                                }
                            }, 350L)
                        }
                    }, 350L)
                }
            }, 400L)
        }
    }
}
