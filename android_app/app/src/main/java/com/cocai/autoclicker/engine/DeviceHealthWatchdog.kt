package com.cocai.autoclicker.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

/**
 * 🔋 Device Health & Thermal Watchdog
 *
 * Monitors battery temperature and charge levels to protect the user's phone from
 * overheating, battery degradation, and OS thermal throttling during 24/7 farming.
 */
class DeviceHealthWatchdog(private val context: Context) {

    var currentTemperatureCelsius: Float = 30.0f
        private set
    var batteryPercentage: Int = 100
        private set
    var isCharging: Boolean = true
        private set

    var onThermalThrottle: ((Float) -> Unit)? = null
    var onLowBatteryHalt: ((Int) -> Unit)? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent?) {
            if (intent == null) return

            val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            currentTemperatureCelsius = rawTemp / 10.0f

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryPercentage = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL

            Log.d("HealthWatchdog", "🔋 Battery: $batteryPercentage% | Temp: ${currentTemperatureCelsius}°C | Charging: $isCharging")

            checkSafetyLimits()
        }
    }

    fun startMonitoring() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        Log.i("HealthWatchdog", "🛡️ Device Health & Thermal Watchdog Online")
    }

    fun stopMonitoring() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.w("HealthWatchdog", "Receiver not registered or already stopped")
        }
    }

    private fun checkSafetyLimits() {
        // Rule 1: High Temperature Watchdog (> 42°C)
        if (currentTemperatureCelsius >= 42.0f) {
            Log.w("HealthWatchdog", "🔥 [THERMAL THROTTLE] Device temperature reached ${currentTemperatureCelsius}°C! Triggering 10m pause.")
            onThermalThrottle?.invoke(currentTemperatureCelsius)
        }

        // Rule 2: Low Battery Protection (< 15% and not charging)
        if (batteryPercentage <= 15 && !isCharging) {
            Log.w("HealthWatchdog", "⚠️ [LOW BATTERY] Battery is at $batteryPercentage% without charger! Pausing bot.")
            onLowBatteryHalt?.invoke(batteryPercentage)
        }
    }
}
