package com.cocai.autoclicker.engine

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import org.json.JSONArray
import org.json.JSONObject

/**
 * 🎬 Gesture Recorder & Playback Engine
 * Allows players to record their own custom attack deployment and replay it automatically.
 */
class GestureRecorderEngine(
    private val context: Context,
    private val accessibilityService: AutoClickAccessibilityService
) {
    data class RecordedEvent(val timestampMs: Long, val type: String, val x: Float, val y: Float)

    private val prefs: SharedPreferences = context.getSharedPreferences("recorded_attack_prefs", Context.MODE_PRIVATE)
    private val recordedEvents = mutableListOf<RecordedEvent>()
    private var recordStartTime: Long = 0
    var isRecording: Boolean = false
        private set

    private val handler = Handler(Looper.getMainLooper())
    private val jitter = GaussianMotionCalibrator()

    fun startRecording() {
        recordedEvents.clear()
        recordStartTime = System.currentTimeMillis()
        isRecording = true
        Log.i("GestureRecorder", "🔴 Started recording custom attack gestures...")
    }

    fun recordTouch(x: Float, y: Float, type: String = "TAP") {
        if (!isRecording) return
        val elapsed = System.currentTimeMillis() - recordStartTime
        recordedEvents.add(RecordedEvent(elapsed, type, x, y))
    }

    fun stopRecording() {
        isRecording = false
        saveRecordedAttack()
        Log.i("GestureRecorder", "⏹ Saved ${recordedEvents.size} recorded gesture events.")
    }

    private fun saveRecordedAttack() {
        val array = JSONArray()
        for (ev in recordedEvents) {
            val obj = JSONObject().apply {
                put("time", ev.timestampMs)
                put("type", ev.type)
                put("x", ev.x.toDouble())
                put("y", ev.y.toDouble())
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_attack_json", array.toString()).apply()
    }

    fun hasSavedAttack(): Boolean {
        val json = prefs.getString("saved_attack_json", "") ?: ""
        return json.isNotEmpty() && json != "[]"
    }

    fun replayAttack(onComplete: () -> Unit) {
        val json = prefs.getString("saved_attack_json", "") ?: ""
        if (json.isEmpty()) {
            onComplete()
            return
        }

        val array = JSONArray(json)
        if (array.length() == 0) {
            onComplete()
            return
        }

        Log.i("GestureRecorder", "▶ Replaying custom recorded attack (${array.length()} actions)...")

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val delay = item.getLong("time")
            val x = item.getDouble("x").toFloat()
            val y = item.getDouble("y").toFloat()

            handler.postDelayed({
                val point = PointF(x, y)
                accessibilityService.performPercentageTap(point, durationMs = 50L)
            }, delay)
        }

        val lastDelay = array.getJSONObject(array.length() - 1).getLong("time")
        handler.postDelayed({
            onComplete()
        }, lastDelay + 1000L)
    }
}
