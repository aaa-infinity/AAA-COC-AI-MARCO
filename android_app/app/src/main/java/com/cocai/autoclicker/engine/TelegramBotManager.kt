package com.cocai.autoclicker.engine

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * 🤖 2-Way Conversational Telegram Remote Control Hub
 *
 * 1. Dispatches live loot reports and screenshots to user chat
 * 2. Long-polls getUpdates for remote commands (/status, /pause, /resume, /attack, /walls, /schedule)
 * 3. Conversational AI fallback for natural chat on Telegram
 */
class TelegramBotManager(
    private val context: Context,
    var botToken: String = "8841143616:AAGbcJKf3MLTN17-tpmwhZKZQIIbErDT1PA",
    var chatId: String = "-1004447017934"
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPolling = false
    private var lastUpdateId = 0L

    var onCommandReceived: ((command: String, args: String) -> String)? = null

    fun sendMessage(text: String, onComplete: ((Boolean) -> Unit)? = null) {
        thread {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://api.telegram.org/bot$botToken/sendMessage")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("chat_id", chatId)
                    put("text", text)
                    put("parse_mode", "HTML")
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload.toString()) }

                val success = conn.responseCode in 200..299
                mainHandler.post { onComplete?.invoke(success) }
            } catch (e: Exception) {
                Log.e("TelegramBot", "Send message error: ${e.message}")
                mainHandler.post { onComplete?.invoke(false) }
            } finally {
                conn?.disconnect()
            }
        }
    }

    fun startCommandPolling() {
        if (isPolling) return
        isPolling = true
        Log.i("TelegramBot", "📡 2-Way Telegram Remote Command Polling Started")

        thread {
            while (isPolling) {
                var conn: HttpURLConnection? = null
                try {
                    val urlStr = "https://api.telegram.org/bot$botToken/getUpdates?offset=${lastUpdateId + 1}&timeout=15"
                    val url = URL(urlStr)
                    conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 20000
                    conn.readTimeout = 20000

                    if (conn.responseCode in 200..299) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val responseStr = reader.readText()
                        reader.close()

                        val json = JSONObject(responseStr)
                        if (json.optBoolean("ok")) {
                            val results = json.optJSONArray("result") ?: JSONArray()
                            for (i in 0 until results.length()) {
                                val item = results.getJSONObject(i)
                                lastUpdateId = item.optLong("update_id", lastUpdateId)

                                val messageObj = item.optJSONObject("message")
                                val text = messageObj?.optString("text")?.trim() ?: ""
                                val senderChatId = messageObj?.optJSONObject("chat")?.optString("id") ?: chatId

                                if (text.isNotEmpty()) {
                                    handleIncomingMessage(text, senderChatId)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("TelegramBot", "Polling transient error: ${e.message}")
                    Thread.sleep(3000L)
                } finally {
                    conn?.disconnect()
                }
            }
        }
    }

    fun stopCommandPolling() {
        isPolling = false
    }

    private fun handleIncomingMessage(text: String, targetChatId: String) {
        val parts = text.split(" ", limit = 2)
        val command = parts[0].lowercase()
        val args = if (parts.size > 1) parts[1] else ""

        Log.i("TelegramBot", "📩 Received Telegram Command: '$command' from $targetChatId")

        val replyText = when {
            command == "/start" || command == "/help" -> {
                """
                👑 <b>Ai Marco coc - Remote Control Bot</b>
                
                <b>Commands:</b>
                • <code>/status</code> - Live status, battery, temp, and raid stats
                • <code>/pause</code> - Pause macro automation
                • <code>/resume</code> - Resume farming loop
                • <code>/attack</code> - Trigger instant search & attack
                • <code>/walls</code> - Execute builder wall upgrade dump
                • <code>/schedule 01:00-06:00</code> - Set farming hours
                • Or chat directly with Ari AI anytime!
                """.trimIndent()
            }
            onCommandReceived != null -> {
                onCommandReceived?.invoke(command, args) ?: "✓ Command received."
            }
            else -> {
                "🤖 Ari AI is online and active in your village!"
            }
        }

        // Send Reply
        sendMessageToChat(targetChatId, replyText)
    }

    private fun sendMessageToChat(targetChatId: String, text: String) {
        thread {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("https://api.telegram.org/bot$botToken/sendMessage")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.doOutput = true

                val payload = JSONObject().apply {
                    put("chat_id", targetChatId)
                    put("text", text)
                    put("parse_mode", "HTML")
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload.toString()) }
                conn.responseCode
            } catch (e: Exception) {
                Log.e("TelegramBot", "Reply error: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
    }
}
