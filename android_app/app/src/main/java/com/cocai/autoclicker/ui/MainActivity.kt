package com.cocai.autoclicker.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.AiMemoryEngine
import com.cocai.autoclicker.engine.ApiKeyRotator
import com.cocai.autoclicker.engine.LiveModelFetcher
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import com.cocai.autoclicker.service.FloatingOverlayService

class MainActivity : Activity() {

    private lateinit var tabBtnDashboard: Button
    private lateinit var tabBtnAi: Button
    private lateinit var tabContentDashboard: View
    private lateinit var tabContentAi: View

    private lateinit var tvAccStatus: TextView
    private lateinit var tvMemoryStats: TextView
    private lateinit var tvActiveKeysCount: TextView
    private lateinit var etApiKeyInput: EditText
    private lateinit var spinnerProvider: Spinner
    private lateinit var spinnerLiveModels: Spinner

    private lateinit var keyRotator: ApiKeyRotator
    private lateinit var memoryEngine: AiMemoryEngine
    private val modelFetcher = LiveModelFetcher()

    private val providers = listOf(
        "Google Gemini (Free Tier)",
        "Groq (Fast Llama-3.3)",
        "OpenRouter (Multi-Provider)",
        "OpenAI (GPT-4o mini)"
    )

    private val providerUrls = listOf(
        "https://generativelanguage.googleapis.com",
        "https://api.groq.com",
        "https://openrouter.ai",
        "https://api.openai.com"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        keyRotator = ApiKeyRotator(this)
        memoryEngine = AiMemoryEngine(this)

        initViews()
        setupTabSwitching()
        setupAiProviderTab()
        updateKeyPoolCount()
    }

    private fun initViews() {
        tabBtnDashboard = findViewById(R.id.tab_btn_dashboard)
        tabBtnAi = findViewById(R.id.tab_btn_ai)
        tabContentDashboard = findViewById(R.id.tab_content_dashboard)
        tabContentAi = findViewById(R.id.tab_content_ai)

        tvAccStatus = findViewById(R.id.tv_accessibility_status)
        tvMemoryStats = findViewById(R.id.tv_memory_stats)
        tvActiveKeysCount = findViewById(R.id.tv_active_keys_count)
        etApiKeyInput = findViewById(R.id.et_api_key_input)
        spinnerProvider = findViewById(R.id.spinner_provider)
        spinnerLiveModels = findViewById(R.id.spinner_live_models)

        findViewById<Button>(R.id.btn_grant_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btn_grant_overlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
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
            Toast.makeText(this, "🚀 Dragon Farm HUD Launched! Open Clash of Clans.", Toast.LENGTH_SHORT).show()
            moveTaskToBack(true)
        }
    }

    private fun setupTabSwitching() {
        tabBtnDashboard.setOnClickListener {
            tabContentDashboard.visibility = View.VISIBLE
            tabContentAi.visibility = View.GONE
            tabBtnDashboard.setBackgroundColor(0xFF2563EB.toInt())
            tabBtnAi.setBackgroundColor(0xFF1E293B.toInt())
        }

        tabBtnAi.setOnClickListener {
            tabContentDashboard.visibility = View.GONE
            tabContentAi.visibility = View.VISIBLE
            tabBtnAi.setBackgroundColor(0xFF2563EB.toInt())
            tabBtnDashboard.setBackgroundColor(0xFF1E293B.toInt())
        }
    }

    private fun setupAiProviderTab() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        spinnerProvider.adapter = adapter

        findViewById<Button>(R.id.btn_add_api_key).setOnClickListener {
            val key = etApiKeyInput.text.toString().trim()
            if (key.isNotEmpty()) {
                keyRotator.addKey(key)
                etApiKeyInput.text.clear()
                updateKeyPoolCount()
                Toast.makeText(this, "Key added to rotation pool!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_fetch_models).setOnClickListener {
            val activeKey = keyRotator.getActiveKey()
            if (activeKey == null) {
                Toast.makeText(this, "Please add an API key first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val selectedIdx = spinnerProvider.selectedItemPosition
            val url = providerUrls.getOrElse(selectedIdx) { providerUrls[0] }

            Toast.makeText(this, "Fetching live models...", Toast.LENGTH_SHORT).show()
            modelFetcher.fetchLiveModels(
                providerUrl = url,
                apiKey = activeKey,
                onSuccess = { models ->
                    val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, models)
                    spinnerLiveModels.adapter = modelAdapter
                    Toast.makeText(this, "Loaded ${models.size} live models!", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(this, "Fetch Error: $err", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun updateKeyPoolCount() {
        val keys = keyRotator.getAllKeys()
        tvActiveKeysCount.text = "API Key Pool: ${keys.size} Key(s) Loaded (Auto-Rotation on 429 Active)"
    }

    override fun onResume() {
        super.onResume()
        tvAccStatus.text = if (AutoClickAccessibilityService.isServiceRunning) {
            "Accessibility Service: ENABLED (Ready)"
        } else {
            "Accessibility Service: DISABLED (Action Required)"
        }

        val bestSide = memoryEngine.getOptimalEntrySide()
        val stats = memoryEngine.getSuccessStatistics()
        tvMemoryStats.text = "Total Raids Learned: ${stats.optInt("total_raids", 0)} | Optimal Attack Angle: $bestSide\nWin Rate: ${stats.optString("win_rate", "100%")} | Auto Key Rotation: ACTIVE"
    }
}
