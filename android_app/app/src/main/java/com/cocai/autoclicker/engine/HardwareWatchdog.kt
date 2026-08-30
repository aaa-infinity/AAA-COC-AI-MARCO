package com.cocai.autoclicker.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

class HardwareWatchdog(private val context: Context) {
    var batteryPercentage: Int = 100
        private set
    var isCharging: Boolean = false
        private set
    var currentTemperatureCelsius: Float = 28.0f
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryPercentage = (level * 100) / scale
                }
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                currentTemperatureCelsius = tempTenths / 10.0f
            }
        }
    }

    fun startMonitoring() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            Log.w("HardwareWatchdog", "Error registering receiver: ${e.message}")
        }
    }

    fun stopMonitoring() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }

    fun isSafeToOperate(): Boolean {
        if (currentTemperatureCelsius >= 42.0f) return false
        if (!isCharging && batteryPercentage < 15) return false
        return true
    }
}
