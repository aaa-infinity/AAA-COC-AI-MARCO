package com.cocai.autoclicker.engine

import android.content.Context
import android.util.Log
import java.util.*

/**
 * ⏰ Smart Sleep / Schedule Timer Engine
 *
 * Configures humanized farming windows (e.g. 01:00 AM to 06:00 AM)
 * and periodic rest breaks (e.g. 30 minutes rest every 2 hours) to avoid 24/7 detection patterns.
 */
class SmartSleepScheduleEngine(private val context: Context) {

    var isScheduleEnabled: Boolean = false
    var startHour: Int = 1     // 01:00 AM
    var startMinute: Int = 0
    var endHour: Int = 6       // 06:00 AM
    var endMinute: Int = 0

    var intervalWorkMinutes: Int = 120  // Work 2 hours
    var intervalRestMinutes: Int = 30   // Rest 30 mins

    var lastSessionStartTime: Long = System.currentTimeMillis()

    fun isWithinScheduleWindow(): Boolean {
        if (!isScheduleEnabled) return true // 24/7 continuous farming if disabled

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            currentTotalMinutes in startTotalMinutes..endTotalMinutes
        } else {
            // Overnight window e.g. 23:00 to 06:00
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
        }
    }

    fun isRestIntervalDue(): Boolean {
        if (!isScheduleEnabled) return false
        val elapsedMinutes = (System.currentTimeMillis() - lastSessionStartTime) / (60 * 1000L)
        return elapsedMinutes >= intervalWorkMinutes
    }

    fun parseScheduleCommand(timeRange: String): Boolean {
        return try {
            // e.g. "01:00-06:00" or "1:00-6:00"
            val parts = timeRange.split("-")
            val startParts = parts[0].trim().split(":")
            val endParts = parts[1].trim().split(":")

            startHour = startParts[0].toInt()
            startMinute = startParts[1].toInt()
            endHour = endParts[0].toInt()
            endMinute = endParts[1].toInt()
            isScheduleEnabled = true

            Log.i("ScheduleEngine", "✓ Schedule configured: $startHour:$startMinute to $endHour:$endMinute")
            true
        } catch (e: Exception) {
            Log.e("ScheduleEngine", "Failed to parse schedule: $timeRange")
            false
        }
    }
}
