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
    private lateinit var visionEngine: ScreenshotVisionEngine
    private lateinit var modelFetcher: LiveModelFetcher
    private lateinit var clanEngine: ClanWarAutomationEngine
    private lateinit var tacticsEngine: AdvancedTacticsEngine
    private lateinit var memoryEngine: AiMemoryEngine
    private lateinit var telemetryBridge: WebTelemetryBridge

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
    private lateinit var etApiKeyInput: EditText
    private lateinit var tvActiveKeysCount: TextView
    private lateinit var tvVisionStatus: TextView

    private val providers = arrayOf(
        "Google Gemini (Official / Recommended)",
        "Groq Fast Inference (Llama 3.3)",
        "OpenRouter Multi-Model",
        "DeepSeek API (V3 / R1)"
    )

    private val providerUrls = arrayOf(
        "https://generativelanguage.googleapis.com/v1beta",
        "https://api.groq.com/openai/v1",
        "https://openrouter.ai/api/v1",
        "https://api.deepseek.com/v1"
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
        visionEngine = ScreenshotVisionEngine(keyRotator)
        modelFetcher = LiveModelFetcher()
        telemetryBridge = WebTelemetryBridge()
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
        etApiKeyInput = findViewById(R.id.et_api_key_input)
        tvActiveKeysCount = findViewById(R.id.tv_active_keys_count)
        tvVisionStatus = findViewById(R.id.tv_vision_status)
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
        findViewById<Button>(R.id.btn_select_th17_overgrowth).setOnClickListener {
            Toast.makeText(this, "TH17 Root Rider + Overgrowth Smash active!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_select_zap_dragons).setOnClickListener {
            Toast.makeText(this, "TH11-TH14 Zap Dragon Farm active!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_select_edrag_spam).setOnClickListener {
            Toast.makeText(this, "Electro Dragon Spam active!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_select_dragon_riders).setOnClickListener {
            Toast.makeText(this, "TH15-TH16 Dragon Rider Smash active!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_select_sneaky_goblins).setOnClickListener {
            Toast.makeText(this, "Sneaky Goblin Ore & Resource Farm active!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClanTab() {
        findViewById<Button>(R.id.btn_trigger_instant_wall_dump).setOnClickListener {
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
    }

    private fun setupAiProviderTab() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        spinnerProvider.adapter = adapter

        val defaultModels = arrayOf("gemini-2.0-flash", "llama-3.3-70b", "deepseek-chat")
        val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultModels)
        spinnerLiveModels.adapter = modelAdapter

        findViewById<Button>(R.id.btn_add_api_key).setOnClickListener {
            val key = etApiKeyInput.text.toString().trim()
            if (key.isNotEmpty()) {
                keyRotator.addKey(key)
                etApiKeyInput.text.clear()
                updateKeyPoolCount()
                Toast.makeText(this, "Key added to rotation pool!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateKeyPoolCount() {
        val keys = keyRotator.getAllKeys()
        tvActiveKeysCount.text = "API Key Pool: ${keys.size} Key(s) Loaded (Auto-Rotation Active)"
    }
}
