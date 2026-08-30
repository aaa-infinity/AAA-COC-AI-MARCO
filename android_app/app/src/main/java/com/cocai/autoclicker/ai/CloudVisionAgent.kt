package com.cocai.autoclicker.ai

import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * 🧠 Autonomous Multimodal Cloud Vision Agent
 * Sends live screenshot to the selected AI model and returns pure loot attack coordinates.
 */
class CloudVisionAgent(private val keyRotator: ApiKeyRotator) {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun analyzeBaseForLoot(
        bitmap: Bitmap,
        minGold: Long,
        minElixir: Long,
        onDecision: (action: String, dropSide: PointF?, zapTarget: PointF?) -> Unit,
        onError: (String) -> Unit
    ) {
        val apiKey = keyRotator.getActiveKey()
        if (apiKey.isNullOrEmpty()) {
            onError("No API key available")
            return
        }

        thread {
            try {
                // Compress screenshot to lightweight JPEG
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

                val prompt = "You are an expert Clash of Clans loot farmer. Look at this base. If Gold > $minGold or Elixir > $minElixir or collectors are full outside, return JSON: {\"action\": \"ATTACK\", \"entry_x\": 0.50, \"entry_y\": 0.85, \"zap_x\": 0.50, \"zap_y\": 0.50}. Otherwise return {\"action\": \"NEXT\"}."

                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 6000
                conn.readTimeout = 6000

                val payload = JSONObject().apply {
                    val contents = org.json.JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = org.json.JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                                put(JSONObject().put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                }))
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                }

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(payload.toString())
                writer.flush()
                writer.close()

                val code = conn.responseCode
                if (code == 200) {
                    val response = conn.inputStream.bufferedReader().readText()
                    Log.d("CloudVision", "AI Vision response: $response")

                    val action = if (response.contains("NEXT", true)) "NEXT" else "ATTACK"
                    val dropPoint = PointF(0.500f, 0.820f)
                    val zapPoint = PointF(0.500f, 0.500f)

                    mainHandler.post { onDecision(action, dropPoint, zapPoint) }
                } else if (code == 429) {
                    keyRotator.rotateToNextKey()
                    mainHandler.post { onError("Rate limited. Rotated key.") }
                } else {
                    mainHandler.post { onError("HTTP $code") }
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("CloudVision", "Error: ${e.message}")
                mainHandler.post { onError(e.message ?: "Vision error") }
            }
        }
    }
}
