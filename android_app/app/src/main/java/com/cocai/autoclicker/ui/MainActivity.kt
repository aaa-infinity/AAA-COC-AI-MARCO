package com.cocai.autoclicker.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.*
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import com.cocai.autoclicker.service.FloatingHUDService

/**
 * 👑 Ai Marco coc - Main Setup Wizard & Cyber Hub
 */
class MainActivity : AppCompatActivity() {

    private lateinit var keyRotator: ApiKeyRotator
    private lateinit var appPrefs: SharedPreferences
    private val pingEngine = ApiKeyPingEngine()
    private val modelDiscovery = ModelDiscoveryClient()

    private var btnGrantAccessibility: Button? = null
    private var btnGrantOverlay: Button? = null

    private var spinnerProvider: Spinner? = null
    private var spinnerLiveModels: Spinner? = null
    private var etApiKeyInput: EditText? = null
    private var tvPingResult: TextView? = null

    private var etTelegramToken: EditText? = null
    private var etTelegramChatId: EditText? = null

    private val providers = arrayOf(
        "Google Gemini (Official / Recommended)",
        "OpenRouter Multi-Model Vision",
        "Groq Fast Vision Inference",
        "DeepSeek / OpenAI Compatible"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            appPrefs = getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
            keyRotator = ApiKeyRotator(this)

            bindViews()
            setupPermissionControls()
            setupAiProviderHub()
            setupTelegramControls()
            setupLaunchController()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updatePermissionButtonStates()
        } catch (e: Exception) {
            Log.w("MainActivity", "Error updating permissions: ${e.message}")
        }
    }

    private fun bindViews() {
        btnGrantAccessibility = findViewById(R.id.btn_grant_accessibility)
        btnGrantOverlay = findViewById(R.id.btn_grant_overlay)

        spinnerProvider = findViewById(R.id.spinner_provider)
        spinnerLiveModels = findViewById(R.id.spinner_live_models)
        etApiKeyInput = findViewById(R.id.et_api_key_input)
        tvPingResult = findViewById(R.id.tv_ping_result)

        etTelegramToken = findViewById(R.id.et_telegram_bot_token)
        etTelegramChatId = findViewById(R.id.et_telegram_chat_id)

        val savedToken = appPrefs.getString("tg_token", "8779968206:AAEE8lhp1ASBvrLiApEgXObYMQlXasWRSKI")
        val savedChatId = appPrefs.getString("tg_chat_id", "@aaafreecloud")
        etTelegramToken?.setText(savedToken)
        etTelegramChatId?.setText(savedChatId)
    }

    private fun updatePermissionButtonStates() {
        if (AutoClickAccessibilityService.isServiceRunning) {
            btnGrantAccessibility?.text = "✓ 2. ACCESSIBILITY ON"
            btnGrantAccessibility?.setBackgroundResource(R.drawable.bg_btn_emerald)
        } else {
            btnGrantAccessibility?.text = "2. ACCESSIBILITY"
            btnGrantAccessibility?.setBackgroundResource(R.drawable.bg_btn_primary)
        }

        val overlayOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true
        if (overlayOk) {
            btnGrantOverlay?.text = "✓ 1. OVERLAY ON"
            btnGrantOverlay?.setBackgroundResource(R.drawable.bg_btn_emerald)
        } else {
            btnGrantOverlay?.text = "1. OVERLAY"
            btnGrantOverlay?.setBackgroundResource(R.drawable.bg_btn_primary)
        }
    }

    private fun setupPermissionControls() {
        btnGrantOverlay?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "✓ Overlay permission already active!", Toast.LENGTH_SHORT).show()
            }
        }

        btnGrantAccessibility?.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Enable 'Ai Marco coc' Accessibility Service", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAiProviderHub() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        spinnerProvider?.adapter = adapter

        // Add Key
        findViewById<Button>(R.id.btn_add_api_key)?.setOnClickListener {
            val key = etApiKeyInput?.text?.toString()?.trim() ?: ""
            if (key.isNotEmpty()) {
                keyRotator.addKey(key)
                etApiKeyInput?.text?.clear()
                Toast.makeText(this, "✓ Key added to auto-rotation pool!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please paste an API key first", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch Live Models & Test Ping
        findViewById<Button>(R.id.btn_ping_api_key)?.setOnClickListener {
            val inputKey = etApiKeyInput?.text?.toString()?.trim() ?: ""
            val activeKey = if (inputKey.isNotEmpty()) inputKey else (keyRotator.getActiveKey() ?: "")

            if (activeKey.isEmpty()) {
                Toast.makeText(this, "Paste an API key first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIdx = spinnerProvider?.selectedItemPosition ?: 0
            val providerEnum = when (selectedIdx) {
                0 -> AiProviderEnum.GOOGLE_AI_STUDIO
                1 -> AiProviderEnum.OPENROUTER
                2 -> AiProviderEnum.GROQ
                else -> AiProviderEnum.CUSTOM_OPENAI
            }

            tvPingResult?.text = "⏳ Connecting to ${providerEnum.displayName} for live models..."
            tvPingResult?.setTextColor(0xFFF59E0B.toInt())

            modelDiscovery.fetchLiveModels(
                provider = providerEnum,
                apiKey = activeKey,
                onSuccess = { liveModels ->
                    val liveAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, liveModels)
                    spinnerLiveModels?.adapter = liveAdapter

                    val firstModel = liveModels.firstOrNull() ?: "gemini-2.0-flash"
                    val providerUrl = when (providerEnum) {
                        AiProviderEnum.GOOGLE_AI_STUDIO -> "https://generativelanguage.googleapis.com/v1beta"
                        AiProviderEnum.GROQ -> "https://api.groq.com/openai/v1"
                        AiProviderEnum.OPENROUTER -> "https://openrouter.ai/api/v1"
                        AiProviderEnum.CUSTOM_OPENAI -> "https://api.deepseek.com/v1"
                    }

                    pingEngine.pingProvider(
                        providerUrl = providerUrl,
                        apiKey = activeKey,
                        modelName = firstModel,
                        onSuccess = { latency, _ ->
                            tvPingResult?.text = "⚡ Loaded ${liveModels.size} Live Models | Ping: ${latency}ms ($firstModel)"
                            tvPingResult?.setTextColor(0xFF34D399.toInt())
                        },
                        onError = { errMsg ->
                            tvPingResult?.text = "⚡ Loaded ${liveModels.size} Live Models ($firstModel) | $errMsg"
                            tvPingResult?.setTextColor(0xFF38BDF8.toInt())
                        }
                    )
                },
                onError = { err ->
                    tvPingResult?.text = "❌ Error: $err"
                    tvPingResult?.setTextColor(0xFFEF4444.toInt())
                }
            )
        }
    }

    private fun setupTelegramControls() {
        findViewById<Button>(R.id.btn_send_telegram_test)?.setOnClickListener {
            val token = etTelegramToken?.text?.toString()?.trim() ?: ""
            val chatId = etTelegramChatId?.text?.toString()?.trim() ?: ""
            if (token.isNotEmpty() && chatId.isNotEmpty()) {
                appPrefs.edit().putString("tg_token", token).putString("tg_chat_id", chatId).apply()
                val bot = TelegramBotManager(this, token, chatId)
                bot.sendMessage("👑 <b>[Ai Marco coc]</b> Connected! Send <code>/status</code>, <code>/pause</code>, <code>/resume</code>, <code>/attack</code>, <code>/walls</code>, <code>/schedule</code>.") { ok ->
                    if (ok) {
                        Toast.makeText(this, "✓ Telegram Test Ping Dispatched!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "⚠️ Telegram Notice: Ensure bot is Admin in channel.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupLaunchController() {
        findViewById<Button>(R.id.btn_start_floating_hud)?.setOnClickListener {
            if (!AutoClickAccessibilityService.isServiceRunning) {
                Toast.makeText(this, "Please enable Accessibility Service first!", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant Overlay Permission!", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                return@setOnClickListener
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this, FloatingHUDService::class.java))
                } else {
                    startService(Intent(this, FloatingHUDService::class.java))
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error launching FloatingHUDService: ${e.message}")
            }

            val launchIntent = packageManager.getLaunchIntentForPackage("com.supercell.clashofclans")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                Toast.makeText(this, "🚀 Launching Clash of Clans...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🚀 Macrorify Controller Launched! Open Clash of Clans.", Toast.LENGTH_SHORT).show()
                moveTaskToBack(true)
            }
        }
    }
}
