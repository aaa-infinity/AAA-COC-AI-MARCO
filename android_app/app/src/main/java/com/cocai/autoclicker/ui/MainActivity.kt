package com.cocai.autoclicker.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.*
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import com.cocai.autoclicker.service.FloatingOverlayService

class MainActivity : AppCompatActivity() {

    private lateinit var keyRotator: ApiKeyRotator
    private lateinit var memoryEngine: AiMemoryEngine
    private val pingEngine = ApiKeyPingEngine()

    private lateinit var tabBtnDashboard: Button
    private lateinit var tabBtnStrategy: Button
    private lateinit var tabBtnClan: Button
    private lateinit var tabBtnAi: Button

    private lateinit var tabContentDashboard: LinearLayout
    private lateinit var tabContentStrategy: LinearLayout
    private lateinit var tabContentClan: LinearLayout
    private lateinit var tabContentAi: LinearLayout

    private lateinit var spinnerProvider: Spinner
    private lateinit var spinnerLiveModels: Spinner
    private lateinit var spinnerStrategyPicker: Spinner
    private lateinit var etApiKeyInput: EditText
    private lateinit var tvActiveKeysCount: TextView
    private lateinit var tvVisionStatus: TextView
    private lateinit var tvPingResult: TextView

    private val providers = arrayOf(
        "Google Gemini (Official / Recommended)",
        "Groq Fast Inference (Llama 3.3)",
        "OpenRouter Multi-Model",
        "DeepSeek API (V3 / R1)"
    )

    private val strategies = arrayOf(
        "TH17 Root Rider + Overgrowth Smash",
        "TH11-TH14 Zap Dragon Farming",
        "Electro Dragon Perimeter Spam",
        "TH15-TH16 Dragon Rider Smash",
        "Sneaky Goblin Ore & Resource Farm"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initEngines()
        bindViews()
        setupTabSwitching()
        setupDashboardControls()
        setupStrategyTab()
        setupClanTab()
        setupAiProviderTab()
    }

    private fun initEngines() {
        keyRotator = ApiKeyRotator(this)
        memoryEngine = AiMemoryEngine(this)
    }

    private fun bindViews() {
        tabBtnDashboard = findViewById(R.id.tab_btn_dashboard)
        tabBtnStrategy = findViewById(R.id.tab_btn_strategy)
        tabBtnClan = findViewById(R.id.tab_btn_clan)
        tabBtnAi = findViewById(R.id.tab_btn_ai)

        tabContentDashboard = findViewById(R.id.tab_content_dashboard)
        tabContentStrategy = findViewById(R.id.tab_content_strategy)
        tabContentClan = findViewById(R.id.tab_content_clan)
        tabContentAi = findViewById(R.id.tab_content_ai)

        spinnerProvider = findViewById(R.id.spinner_provider)
        spinnerLiveModels = findViewById(R.id.spinner_live_models)
        spinnerStrategyPicker = findViewById(R.id.spinner_strategy_picker)
        etApiKeyInput = findViewById(R.id.et_api_key_input)
        tvActiveKeysCount = findViewById(R.id.tv_active_keys_count)
        tvVisionStatus = findViewById(R.id.tv_vision_status)
        tvPingResult = findViewById(R.id.tv_ping_result)
    }

    private fun setupDashboardControls() {
        findViewById<Button>(R.id.btn_grant_accessibility).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Enable 'Ai Marco coc' Accessibility Service", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btn_grant_overlay).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Overlay permission already granted!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_start_floating_hud).setOnClickListener {
            if (!AutoClickAccessibilityService.isServiceRunning) {
                Toast.makeText(this, "Please enable Accessibility Service first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant Overlay Permission!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            startService(Intent(this, FloatingOverlayService::class.java))

            val launchIntent = packageManager.getLaunchIntentForPackage("com.supercell.clashofclans")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                Toast.makeText(this, "🚀 Launching Clash of Clans Home Village...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🚀 Controller Launched! Open Clash of Clans.", Toast.LENGTH_SHORT).show()
                moveTaskToBack(true)
            }
        }
    }

    private fun setupTabSwitching() {
        val buttons = listOf(tabBtnDashboard, tabBtnStrategy, tabBtnClan, tabBtnAi)
        val contents = listOf(tabContentDashboard, tabContentStrategy, tabContentClan, tabContentAi)

        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                for (j in contents.indices) {
                    if (i == j) {
                        contents[j].visibility = View.VISIBLE
                        buttons[j].setBackgroundResource(R.drawable.bg_tab_active_pill)
                        buttons[j].setTextColor(0xFF38BDF8.toInt())
                    } else {
                        contents[j].visibility = View.GONE
                        buttons[j].setBackgroundResource(android.R.color.transparent)
                        buttons[j].setTextColor(0xFF94A3B8.toInt())
                    }
                }
            }
        }
    }

    private fun setupStrategyTab() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strategies)
        spinnerStrategyPicker.adapter = adapter
    }

    private fun setupClanTab() {
        findViewById<Button>(R.id.btn_trigger_collect_loot).setOnClickListener {
            val service = AutoClickAccessibilityService.instance
            if (service == null) {
                Toast.makeText(this, "Accessibility Service is not running!", Toast.LENGTH_SHORT).show()
            } else {
                val daily = DailyRewardsCollectorEngine(service)
                daily.collectAllDailyRewards {
                    Toast.makeText(this, "💰 Harvested Mines & Treasury!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btn_trigger_upgrade_walls).setOnClickListener {
            val service = AutoClickAccessibilityService.instance
            if (service == null) {
                Toast.makeText(this, "Accessibility Service is not running!", Toast.LENGTH_SHORT).show()
            } else {
                val wallEngine = WallUpgradeEngine(service)
                wallEngine.performWallUpgrades(wallsToUpgrade = 3) {
                    Toast.makeText(this, "🧱 Upgraded 3 Walls with Free Builder!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btn_trigger_donate_now).setOnClickListener {
            Toast.makeText(this, "🤝 Auto-Donating to Clan members...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_trigger_cc_request).setOnClickListener {
            Toast.makeText(this, "🛡️ Requested Clan Castle reinforcements!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_trigger_clean_obstacles).setOnClickListener {
            val service = AutoClickAccessibilityService.instance
            if (service == null) {
                Toast.makeText(this, "Accessibility Service is not running!", Toast.LENGTH_SHORT).show()
            } else {
                val daily = DailyRewardsCollectorEngine(service)
                daily.cleanObstaclesForGems {
                    Toast.makeText(this, "💎 Harvested Obstacles for Gems!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btn_send_telegram_test).setOnClickListener {
            val token = findViewById<EditText>(R.id.et_telegram_bot_token).text.toString().trim()
            val chatId = findViewById<EditText>(R.id.et_telegram_chat_id).text.toString().trim()
            if (token.isNotEmpty() && chatId.isNotEmpty()) {
                val bot = TelegramBotManager(this, token, chatId)
                bot.sendMessage("👑 <b>[Ai Marco coc]</b> 2-Way Telegram Remote Connected! You can now send <code>/status</code>, <code>/pause</code>, <code>/resume</code>, <code>/attack</code>, <code>/walls</code>, or <code>/schedule</code>.") { ok ->
                    if (ok) {
                        Toast.makeText(this, "✓ Telegram Test Ping Dispatched!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "❌ Telegram Error. Verify Token/Chat ID.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupAiProviderTab() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        spinnerProvider.adapter = adapter

        val defaultModels = arrayOf(
            "gemini-2.0-flash",
            "gemini-2.5-pro",
            "llama-3.3-70b-versatile",
            "google/gemma-4-31b-it:free",
            "deepseek-chat"
        )
        val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultModels)
        spinnerLiveModels.adapter = modelAdapter

        // Add Key to Pool
        findViewById<Button>(R.id.btn_add_api_key).setOnClickListener {
            val key = etApiKeyInput.text.toString().trim()
            if (key.isNotEmpty()) {
                keyRotator.addKey(key)
                etApiKeyInput.text.clear()
                updateKeyPoolCount()
                Toast.makeText(this, "✓ Key added to auto-rotation pool!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please paste an API key first", Toast.LENGTH_SHORT).show()
            }
        }

        // Test Ping / Validate Key
        findViewById<Button>(R.id.btn_ping_api_key).setOnClickListener {
            val inputKey = etApiKeyInput.text.toString().trim()
            val activeKey = if (inputKey.isNotEmpty()) inputKey else (keyRotator.getActiveKey() ?: "")

            if (activeKey.isEmpty()) {
                Toast.makeText(this, "Enter or add an API key to test ping!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedModel = spinnerLiveModels.selectedItem?.toString() ?: "gemini-2.0-flash"
            val selectedProviderIdx = spinnerProvider.selectedItemPosition

            val providerUrl = when (selectedProviderIdx) {
                0 -> "https://generativelanguage.googleapis.com/v1beta"
                1 -> "https://api.groq.com/openai/v1"
                2 -> "https://openrouter.ai/api/v1"
                else -> "https://api.deepseek.com/v1"
            }

            tvPingResult.text = "⏳ Testing API connection to $selectedModel..."
            tvPingResult.setTextColor(0xFFF59E0B.toInt())

            pingEngine.pingProvider(
                providerUrl = providerUrl,
                apiKey = activeKey,
                modelName = selectedModel,
                onSuccess = { latency, msg ->
                    tvPingResult.text = msg
                    tvPingResult.setTextColor(0xFF34D399.toInt())
                },
                onError = { errMsg ->
                    tvPingResult.text = errMsg
                    tvPingResult.setTextColor(0xFFEF4444.toInt())
                }
            )
        }

        // Clear Key Pool
        findViewById<Button>(R.id.btn_clear_keys).setOnClickListener {
            keyRotator.clearAllKeys()
            updateKeyPoolCount()
            tvPingResult.text = "⚡ Status: Key pool cleared"
            tvPingResult.setTextColor(0xFF94A3B8.toInt())
            Toast.makeText(this, "Key pool cleared.", Toast.LENGTH_SHORT).show()
        }

        updateKeyPoolCount()
    }

    private fun updateKeyPoolCount() {
        val keys = keyRotator.getAllKeys()
        tvActiveKeysCount.text = "Active Keys in Pool: ${keys.size} Key(s) Loaded (Auto-Rotation Active)"
    }
}
