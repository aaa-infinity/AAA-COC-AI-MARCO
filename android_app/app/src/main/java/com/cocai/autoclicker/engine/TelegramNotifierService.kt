package com.cocai.autoclicker.engine

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class TelegramNotifierService(
    private val botToken: String = "8841143616:AAGbcJKf3MLTN17-tpmwhZKZQIIbErDT1PA",
    private val chatId: String = "-1004447017934"
) {
    private val handler = Handler(Looper.getMainLooper())
    var isEnabled: Boolean = true

    fun sendRaidReport(
        strategy: String,
        goldGained: Long,
        elixirGained: Long,
        darkElixirGained: Long,
        totalRaids: Int
    ) {
        if (!isEnabled || botToken.isEmpty()) return

        thread {
            try {
                val message = """
                    🐉 <b>Ai Marco coc — Raid Report #$totalRaids</b>
                    ⚔️ <b>Strategy:</b> $strategy
                    💰 <b>Gold Farmed:</b> +$goldGained
                    🧪 <b>Elixir Farmed:</b> +$elixirGained
                    💧 <b>Dark Elixir:</b> +$darkElixirGained
                    🖐️ <b>Multi-Touch:</b> 4-Finger Wave Active
                    🛡️ <b>Supervisor:</b> ONLINE
                """.trimIndent()

                val encodedText = URLEncoder.encode(message, "UTF-8")
                val urlStr = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&parse_mode=HTML&text=$encodedText"
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val code = conn.responseCode
                Log.i("TelegramNotifier", "Dispatched raid report to Telegram: HTTP $code")
                conn.disconnect()
            } catch (e: Exception) {
                Log.w("TelegramNotifier", "Could not send report to Telegram: ${e.message}")
            }
        }
    }
}
