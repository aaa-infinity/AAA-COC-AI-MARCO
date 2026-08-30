package com.cocai.autoclicker.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

/**
 * 🤖 2-Way Conversational Telegram Remote Control
 */
class TelegramBotManager(
    private val context: Context,
    private val botToken: String,
    private val defaultChatId: String
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun sendMessage(text: String, onComplete: ((Boolean) -> Unit)? = null) {
        if (botToken.isBlank() || defaultChatId.isBlank()) {
            onComplete?.invoke(false)
            return
        }

        thread {
            try {
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val urlStr = "https://api.telegram.org/bot${botToken.trim()}/sendMessage?chat_id=${defaultChatId.trim()}&parse_mode=HTML&text=$encodedText"
                val conn = URL(urlStr).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 6000
                conn.readTimeout = 6000

                val code = conn.responseCode
                val ok = code in 200..299
                conn.disconnect()
                mainHandler.post { onComplete?.invoke(ok) }
            } catch (e: Exception) {
                Log.e("TelegramBot", "Send error: ${e.message}")
                mainHandler.post { onComplete?.invoke(false) }
            }
        }
    }
}
