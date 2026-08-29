package com.cocai.autoclicker.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
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

    private var isRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent?) {
            if (intent == null) return
            try {
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
            } catch (e: Exception) {
                Log.w("HealthWatchdog", "Error parsing battery intent: ${e.message}")
            }
        }
    }

    fun startMonitoring() {
        if (isRegistered) return
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(batteryReceiver, filter)
            }
            isRegistered = true
            Log.i("HealthWatchdog", "🛡️ Device Health & Thermal Watchdog Online")
        } catch (e: Exception) {
            Log.w("HealthWatchdog", "Warning: Could not register battery receiver: ${e.message}")
        }
    }

    fun stopMonitoring() {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(batteryReceiver)
            isRegistered = false
        } catch (e: Exception) {
            Log.w("HealthWatchdog", "Receiver not registered or already stopped: ${e.message}")
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
