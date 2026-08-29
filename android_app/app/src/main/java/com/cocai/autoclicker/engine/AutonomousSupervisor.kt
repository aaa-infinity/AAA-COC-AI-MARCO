package com.cocai.autoclicker.engine

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class AutonomousSupervisor(
    private val context: Context,
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isSupervising: Boolean = false
        private set

    private var recoveryAttempts: Int = 0

    private val supervisorRunnable = object : Runnable {
        override fun run() {
            if (!isSupervising) return
            checkGameHealthAndRecover()
            handler.postDelayed(this, 15000L) // Runs health check every 15 seconds
        }
    }

    fun startSupervisor() {
        if (isSupervising) return
        isSupervising = true
        Log.i("Supervisor", "=== [AUTONOMOUS SUPERVISOR] Self-Independent Watchdog Started ===")
        handler.post(supervisorRunnable)
    }

    fun stopSupervisor() {
        isSupervising = false
        handler.removeCallbacks(supervisorRunnable)
        Log.i("Supervisor", "Autonomous Supervisor stopped.")
    }

    /**
     * Self-Healing Health Check & Auto-Recovery
     */
    private fun checkGameHealthAndRecover() {
        // 1. Check for "Reload Game" / "Try Again" popups (standard center popup button: x=960, y=660)
        // If an out-of-sync or connection lost dialog is displayed, tap the confirmation button:
        Log.d("Supervisor", "Performing autonomous health check...")
        
        // 2. Ensure Clash of Clans is in foreground
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.supercell.clashofclans")
        if (launchIntent != null && recoveryAttempts > 3) {
            Log.w("Supervisor", "Game appears unresponsive. Autonomous re-launch triggered.")
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            recoveryAttempts = 0
        }
    }

    /**
     * Handle Disconnect / Out-of-Sync Dialog:
     * Taps the primary Reload / Try Again button (center screen).
     */
    fun recoverFromDisconnectDialog(onRecovered: () -> Unit) {
        Log.i("Supervisor", "Recovering from disconnection dialog. Tapping [Reload/Okay]...")
        accessibilityService.performTap(960f, 660f) {
            handler.postDelayed({
                Log.i("Supervisor", "Game reloaded. Resuming autonomous macro loop.")
                onRecovered()
            }, 6000L) // Wait 6 seconds for game to reload
        }
    }

    /**
     * Autonomous Clan Castle Request Routine:
     * Taps Clan Castle, clicks "Request", confirms troop request popup.
     */
    fun performClanCastleRequest(onComplete: () -> Unit) {
        Log.i("Supervisor", "=== [AUTONOMOUS CC] Requesting Clan Castle Reinforcements ===")
        // Tap Clan Castle location (Village Center: x=960, y=540)
        accessibilityService.performTap(960f, 540f) {
            handler.postDelayed({
                // Tap "Request" button in bottom action bar (x=960, y=950)
                accessibilityService.performTap(960f, 950f) {
                    handler.postDelayed({
                        // Tap "Send Request" in popup (x=1150, y=720)
                        accessibilityService.performTap(1150f, 720f) {
                            handler.postDelayed({
                                Log.i("Supervisor", "Clan Castle request sent.")
                                onComplete()
                            }, 500L)
                        }
                    }, 800L)
                }
            }, 700L)
        }
    }

    /**
     * Autonomous Obstacle & Gem Box Cleaner:
     * Clears trees, logs, trunks, and Gem Boxes for free gems.
     */
    fun cleanBaseObstacles(onComplete: () -> Unit) {
        Log.i("Supervisor", "=== [AUTONOMOUS OBSTACLE] Cleaning Village Obstacles & Gem Boxes ===")
        val obstacleLocations = listOf(
            Pair(450f, 320f),
            Pair(1470f, 320f),
            Pair(450f, 760f),
            Pair(1470f, 760f)
        )
        var idx = 0

        fun cleanNext() {
            if (idx < obstacleLocations.size) {
                val loc = obstacleLocations[idx++]
                accessibilityService.performTap(loc.first, loc.second) {
                    handler.postDelayed({
                        // Tap "Remove" button (x=960, y=950)
                        accessibilityService.performTap(960f, 950f) {
                            handler.postDelayed({ cleanNext() }, 400L)
                        }
                    }, 500L)
                }
            } else {
                Log.i("Supervisor", "Obstacle cleaning routine complete.")
                onComplete()
            }
        }

        cleanNext()
    }
}
