package com.cocai.autoclicker.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.*
import com.cocai.autoclicker.service.AutoClickAccessibilityService
import com.cocai.autoclicker.service.FloatingOverlayService

class MainActivity : Activity() {

    private lateinit var tabBtnDashboard: Button
    private lateinit var tabBtnStrategy: Button
    private lateinit var tabBtnClan: Button
    private lateinit var tabBtnAi: Button

    private lateinit var tabContentDashboard: View
    private lateinit var tabContentStrategy: View
    private lateinit var tabContentClan: View
    private lateinit var tabContentAi: View

    private lateinit var tvAccStatus: TextView
    private lateinit var tvMemoryStats: TextView
    private lateinit var tvActiveKeysCount: TextView
    private lateinit var tvVisionStatus: TextView
    private lateinit var etApiKeyInput: EditText
    private lateinit var spinnerProvider: Spinner
    private lateinit var spinnerLiveModels: Spinner
    private lateinit var spinnerStrategyPicker: Spinner

    private lateinit var keyRotator: ApiKeyRotator
    private lateinit var memoryEngine: AiMemoryEngine
    private lateinit var visionEngine: ScreenshotVisionEngine
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

    private val strategies = listOf(
        "⚡ Home Village Zap Dragon Farming (4-Finger Multi-Touch)",
        "⚡ Electro Dragon Core Wipeout",
        "🐉 Dragon + Dragon Rider Smash",
        "🏹 Sneaky Goblin Ore & Resource Sniping"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CrashTelemetryService.init(this)

        keyRotator = ApiKeyRotator(this)
        memoryEngine = AiMemoryEngine(this)
        visionEngine = ScreenshotVisionEngine(keyRotator)

        initViews()
        setupTabSwitching()
        setupStrategyAndVillageTabs()
        setupAiProviderTab()
        updateKeyPoolCount()
    }

    private fun initViews() {
        tabBtnDashboard = findViewById(R.id.tab_btn_dashboard)
        tabBtnStrategy = findViewById(R.id.tab_btn_strategy)
        tabBtnClan = findViewById(R.id.tab_btn_clan)
        tabBtnAi = findViewById(R.id.tab_btn_ai)

        tabContentDashboard = findViewById(R.id.tab_content_dashboard)
        tabContentStrategy = findViewById(R.id.tab_content_strategy)
        tabContentClan = findViewById(R.id.tab_content_clan)
        tabContentAi = findViewById(R.id.tab_content_ai)

        tvAccStatus = findViewById(R.id.tv_accessibility_status)
        tvMemoryStats = findViewById(R.id.tv_memory_stats)
        tvActiveKeysCount = findViewById(R.id.tv_active_keys_count)
        tvVisionStatus = findViewById(R.id.tv_vision_status)
        etApiKeyInput = findViewById(R.id.et_api_key_input)
        spinnerProvider = findViewById(R.id.spinner_provider)
        spinnerLiveModels = findViewById(R.id.spinner_live_models)
        spinnerStrategyPicker = findViewById(R.id.spinner_strategy_picker)

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

            val launchIntent = packageManager.getLaunchIntentForPackage("com.supercell.clashofclans")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
                Toast.makeText(this, "🚀 Ai Marco coc: Launching Clash of Clans Home Village...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "🚀 Controller Launched! Open Clash of Clans.", Toast.LENGTH_SHORT).show()
                moveTaskToBack(true)
            }
        }
    }

    private fun setupTabSwitching() {
        val buttons = listOf(tabBtnDashboard, tabBtnStrategy, tabBtnClan, tabBtnAi)
        val contents = listOf(tabContentDashboard, tabContentStrategy, tabContentClan, tabContentAi)

        fun selectTab(idx: Int) {
            for (i in contents.indices) {
                contents[i].visibility = if (i == idx) View.VISIBLE else View.GONE
                buttons[i].setBackgroundColor(if (i == idx) 0xFF2563EB.toInt() else 0xFF131C2E.toInt())
                buttons[i].setTextColor(if (i == idx) 0xFFFFFFFF.toInt() else 0xFF94A3B8.toInt())
            }
        }

        tabBtnDashboard.setOnClickListener { selectTab(0) }
        tabBtnStrategy.setOnClickListener { selectTab(1) }
        tabBtnClan.setOnClickListener { selectTab(2) }
        tabBtnAi.setOnClickListener { selectTab(3) }
    }

    private fun setupStrategyAndVillageTabs() {
        val stratAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, strategies)
        spinnerStrategyPicker.adapter = stratAdapter

        findViewById<Button>(R.id.btn_trigger_donate_now).setOnClickListener {
            val accService = AutoClickAccessibilityService.instance
            if (accService != null) {
                val donate = AutoDonateEngine(accService)
                Toast.makeText(this, "🤝 Scanning Clan Chat for troop requests...", Toast.LENGTH_SHORT).show()
                donate.startAutoDonate {
                    Toast.makeText(this, "✓ Auto-Donate scan complete!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_trigger_cc_request).setOnClickListener {
            val accService = AutoClickAccessibilityService.instance
            if (accService != null) {
                val supervisor = AutonomousSupervisor(this, accService)
                Toast.makeText(this, "🛡️ Requesting Clan Castle troops...", Toast.LENGTH_SHORT).show()
                supervisor.performClanCastleRequest {
                    Toast.makeText(this, "✓ Clan Castle request sent!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_trigger_clean_obstacles).setOnClickListener {
            val accService = AutoClickAccessibilityService.instance
            if (accService != null) {
                val supervisor = AutonomousSupervisor(this, accService)
                Toast.makeText(this, "💎 Clearing Gem Boxes and base obstacles...", Toast.LENGTH_SHORT).show()
                supervisor.cleanBaseObstacles {
                    Toast.makeText(this, "✓ Base obstacles and Gem Boxes cleaned!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_trigger_collect_loot).setOnClickListener {
            val accService = AutoClickAccessibilityService.instance
            if (accService != null) {
                val engine = CocFarmingEngine(accService)
                Toast.makeText(this, "💰 Collecting Mines, Drills and Treasury Ores...", Toast.LENGTH_SHORT).show()
                engine.collectHomeVillageResourcesNow {
                    Toast.makeText(this, "✓ Home Village resources collected!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
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

        findViewById<Button>(R.id.btn_test_vision).setOnClickListener {
            tvVisionStatus.text = "Vision AI: Processing screenshot with Screenshot-to-Code engine..."
            val testBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(testBitmap)
            val paint = Paint().apply { color = Color.DKGRAY }
            canvas.drawRect(0f, 0f, 1920f, 1080f, paint)

            val selectedIdx = spinnerProvider.selectedItemPosition
            val url = providerUrls.getOrElse(selectedIdx) { providerUrls[0] }
            val selectedModel = spinnerLiveModels.selectedItem?.toString() ?: "gemini-2.0-flash"

            visionEngine.analyzeScreenBitmap(
                bitmap = testBitmap,
                providerUrl = url,
                modelName = selectedModel,
                onResult = { result ->
                    tvVisionStatus.text = "✓ Home Village Game State: ${result.gameState}\n✓ Optimal Entry Side: ${result.recommendedEntrySide}\n✓ Zap Targets: ${result.zapTargets}\n✓ 4-Finger Wave: Ready"
                },
                onError = { err ->
                    tvVisionStatus.text = "Vision Fallback: Heuristic model active (${err})"
                }
            )
        }
    }

    private fun updateKeyPoolCount() {
        val keys = keyRotator.getAllKeys()
        tvActiveKeysCount.text = "API Key Pool: ${keys.size} Key(s) Loaded (Auto-Rotation Active)"
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
        tvMemoryStats.text = "Home Village Raids Learned: ${stats.optInt("total_raids", 0)} | Optimal Attack Angle: $bestSide\nMulti-Touch: 4-FINGER | 265+ Real Assets: READY"
    }
}
