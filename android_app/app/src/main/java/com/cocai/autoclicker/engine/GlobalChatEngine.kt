package com.cocai.autoclicker.engine

import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

/**
 * 💬 Global Chat 2.0 & Clan Auto-Recruiter
 *
 * Automatically opens Clan Chat / Recruitment Board and posts recruitment invites.
 */
class GlobalChatEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isRecruiting: Boolean = false
        private set

    private val PCT_CHAT_TAB = PointF(0.015f, 0.500f)           // Clan Chat sidebar expander
    private val PCT_RECRUIT_BTN = PointF(0.200f, 0.880f)        // Send recruitment message
    private val PCT_CLOSE_CHAT = PointF(0.380f, 0.100f)         // Collapse Chat sidebar

    fun broadcastRecruitmentMessage(messagePreset: String = "Active Clan looking for TH12+ players! Daily Wars & Max Clan Games.", onComplete: () -> Unit) {
        if (isRecruiting) {
            onComplete()
            return
        }

        isRecruiting = true
        Log.i("ChatRecruiter", "=== [RECRUITMENT] Broadcasting Clan Recruitment Message ===")

        accessibilityService.performPercentageTap(PCT_CHAT_TAB) {
            handler.postDelayed({
                accessibilityService.performPercentageTap(PCT_RECRUIT_BTN) {
                    handler.postDelayed({
                        accessibilityService.performPercentageTap(PCT_CLOSE_CHAT) {
                            isRecruiting = false
                            Log.i("ChatRecruiter", "✓ Clan recruitment broadcast dispatched.")
                            onComplete()
                        }
                    }, 800L)
                }
            }, Random.nextLong(1000L, 1400L))
        }
    }
}
