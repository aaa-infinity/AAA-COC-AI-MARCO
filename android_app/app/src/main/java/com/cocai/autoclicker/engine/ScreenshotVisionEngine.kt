package com.cocai.autoclicker.engine

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class VisionAnalysisResult(
    val gameState: String,
    val recommendedEntrySide: String,
    val zapTargets: List<Pair<Float, Float>>,
    val rawJson: JSONObject
)

class ScreenshotVisionEngine(
    private val keyRotator: ApiKeyRotator
) {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun analyzeScreenBitmap(
        bitmap: Bitmap,
        providerUrl: String = "https://generativelanguage.googleapis.com",
        modelName: String = "gemini-2.0-flash",
        onResult: (VisionAnalysisResult) -> Unit,
        onError: (String) -> Unit
    ) {
        executor.execute {
            try {
                val activeKey = keyRotator.getActiveKey()
                if (activeKey == null) {
                    // Fallback to local heuristic
                    mainHandler.post {
                        onResult(getFallbackResult())
                    }
                    return@execute
                }

                // Compress bitmap to JPEG Base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val prompt = "Analyze this Clash of Clans screenshot. Return JSON with gameState, recommendedEntrySide, and zapTargets."

                val isGemini = providerUrl.contains("generativelanguage.googleapis.com")
                val endpoint = if (isGemini) {
                    "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$activeKey"
                } else {
                    if (providerUrl.endsWith("/v1")) "$providerUrl/chat/completions" else "$providerUrl/v1/chat/completions"
                }

                val conn = URL(endpoint).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 15000
                conn.readTimeout = 15000

                val payload = if (isGemini) {
                    JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", prompt))
                                    put(JSONObject().put("inline_data", JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64Image)
                                    }))
                                })
                            })
                        })
                    }
                } else {
                    conn.setRequestProperty("Authorization", "Bearer $activeKey")
                    JSONObject().apply {
                        put("model", modelName)
                        put("messages", JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", JSONArray().apply {
                                    put(JSONObject().put("type", "text").put("text", prompt))
                                    put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$base64Image")))
                                })
                            })
                        })
                    }
                }

                conn.doOutput = true
                conn.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }

                val code = conn.responseCode
                if (code == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseStr = reader.readText()
                    reader.close()

                    val result = parseVisionResponse(responseStr, isGemini)
                    mainHandler.post { onResult(result) }
                } else {
                    if (code == 429) {
                        keyRotator.reportRateLimit(activeKey)
                    }
                    mainHandler.post {
                        onResult(getFallbackResult())
                    }
                }
            } catch (e: Exception) {
                Log.e("ScreenshotVision", "Error analyzing screen: ${e.message}")
                mainHandler.post {
                    onResult(getFallbackResult())
                }
            }
        }
    }

    private fun parseVisionResponse(raw: String, isGemini: Boolean): VisionAnalysisResult {
        return try {
            val json = JSONObject(raw)
            val textContent = if (isGemini) {
                json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } else {
                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            }

            val cleaned = textContent.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleaned)
            val state = parsedObj.optString("gameState", "LIVE_BATTLE")
            val angle = parsedObj.optString("recommendedEntrySide", "BOTTOM_LEFT")

            val zapList = mutableListOf<Pair<Float, Float>>()
            if (parsedObj.has("zapTargets")) {
                val zaps = parsedObj.getJSONArray("zapTargets")
                for (i in 0 until zaps.length()) {
                    val pt = zaps.getJSONArray(i)
                    zapList.add(Pair(pt.getDouble(0).toFloat(), pt.getDouble(1).toFloat()))
                }
            }
            if (zapList.isEmpty()) {
                zapList.add(Pair(750f, 480f))
                zapList.add(Pair(1170f, 480f))
            }

            VisionAnalysisResult(state, angle, zapList, parsedObj)
        } catch (e: Exception) {
            getFallbackResult()
        }
    }

    private fun getFallbackResult(): VisionAnalysisResult {
        val fallbackJson = JSONObject().apply {
            put("gameState", "LIVE_BATTLE")
            put("recommendedEntrySide", "BOTTOM_LEFT")
            put("zapTargets", JSONArray().apply {
                put(JSONArray().put(750).put(480))
                put(JSONArray().put(1170).put(480))
            })
        }
        return VisionAnalysisResult(
            gameState = "LIVE_BATTLE",
            recommendedEntrySide = "BOTTOM_LEFT",
            zapTargets = listOf(Pair(750f, 480f), Pair(1170f, 480f)),
            rawJson = fallbackJson
        )
    }
}
