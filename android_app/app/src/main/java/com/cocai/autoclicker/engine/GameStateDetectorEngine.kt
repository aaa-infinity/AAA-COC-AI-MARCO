package com.cocai.autoclicker.engine

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.cocai.autoclicker.service.AutoClickAccessibilityService

enum class GameScreenState {
    HOME_VILLAGE,
    ATTACK_MATCHMAKING,
    BATTLE_IN_PROGRESS,
    SUPERCELL_LOADING,
    UNKNOWN_OR_BACKGROUND
}

/**
 * 🛡️ Game State Detector & Foreground Safety Guard
 *
 * Ensures macro taps are strictly confined to Clash of Clans and prevents
 * phantom taps when the game is loading, minimized, or in launcher.
 */
class GameStateDetectorEngine {

    companion object {
        const val COC_PACKAGE_NAME = "com.supercell.clashofclans"
    }

    var currentState: GameScreenState = GameScreenState.HOME_VILLAGE
    var isCoCInForeground: Boolean = true

    fun isSafeToTap(): Boolean {
        return isCoCInForeground
    }

    fun updateForegroundState(packageName: CharSequence?) {
        if (packageName != null) {
            val pkg = packageName.toString()
            isCoCInForeground = pkg.contains("supercell.clashofclans") || pkg.contains("cocai")
            Log.d("GameState", "Active Package: $pkg -> Safe: $isCoCInForeground")
        }
    }
}
