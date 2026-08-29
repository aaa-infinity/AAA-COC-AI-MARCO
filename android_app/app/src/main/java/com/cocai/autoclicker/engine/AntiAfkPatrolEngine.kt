package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import kotlin.random.Random

class AntiAfkPatrolEngine(
    private val accessibilityService: AutoClickAccessibilityService
) {
    private val handler = Handler(Looper.getMainLooper())
    var isPatrolling: Boolean = false
        private set

    private val patrolRunnable = object : Runnable {
        override fun run() {
            if (!isPatrolling) return
            performVillagePatrolSweep()
            val nextDelay = Random.nextLong(25000L, 45000L) // every 25-45 seconds
            handler.postDelayed(this, nextDelay)
        }
    }

    fun startPatrol() {
        if (isPatrolling) return
        isPatrolling = true
        Log.i("AntiAFK", "=== [ANTI-AFK] Base Patrol Active ===")
        handler.post(patrolRunnable)
    }

    fun stopPatrol() {
        isPatrolling = false
        handler.removeCallbacks(patrolRunnable)
        Log.i("AntiAFK", "Anti-AFK Base Patrol stopped.")
    }

    private fun performVillagePatrolSweep() {
        val sweepType = Random.nextInt(3)
        when (sweepType) {
            0 -> {
                // Gentle horizontal pan
                Log.i("AntiAFK", "Performing horizontal pan sweep...")
                accessibilityService.performBezierSwipe(700f, 500f, 1200f, 520f, 450L)
            }
            1 -> {
                // Gentle vertical pan
                Log.i("AntiAFK", "Performing vertical pan sweep...")
                accessibilityService.performBezierSwipe(960f, 400f, 960f, 700f, 450L)
            }
            2 -> {
                // Gentle diagonal pan
                Log.i("AntiAFK", "Performing diagonal pan sweep...")
                accessibilityService.performBezierSwipe(800f, 400f, 1100f, 650f, 450L)
            }
        }
    }
}
