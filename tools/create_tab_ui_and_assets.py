import os

# 1. Update activity_main.xml with Dual Tab layout (Bot Dashboard + AI Brain API Keys)
activity_main_xml = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#090D16">

    <!-- Top Tab Bar -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:orientation="horizontal"
        android:background="#131C2E">

        <Button
            android:id="@+id/tab_btn_dashboard"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:background="#2563EB"
            android:text="🏰 BOT DASHBOARD"
            android:textColor="#FFFFFF"
            android:textSize="12sp"
            android:textStyle="bold" />

        <Button
            android:id="@+id/tab_btn_ai"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:background="#1E293B"
            android:text="🧠 AI BRAIN &amp; KEYS"
            android:textColor="#94A3B8"
            android:textSize="12sp"
            android:textStyle="bold" />
    </LinearLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <!-- TAB 1 CONTENT: BOT DASHBOARD -->
            <LinearLayout
                android:id="@+id/tab_content_dashboard"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:visibility="visible">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="🐉 AAA COC AI MARCO"
                    android:textColor="#F59E0B"
                    android:textSize="20sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Dedicated Home Village Dragon &amp; Zap Air Assault Auto-Farming."
                    android:textColor="#94A3B8"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/tv_accessibility_status"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:background="#131C2E"
                    android:padding="10dp"
                    android:text="Accessibility Service: CHECKING..."
                    android:textColor="#F59E0B"
                    android:textSize="12sp" />

                <Button
                    android:id="@+id/btn_grant_accessibility"
                    android:layout_width="match_parent"
                    android:layout_height="44dp"
                    android:layout_marginTop="10dp"
                    android:backgroundTint="#2563EB"
                    android:text="1. Grant Accessibility Service (Non-Root)"
                    android:textColor="#FFFFFF"
                    android:textSize="11sp" />

                <Button
                    android:id="@+id/btn_grant_overlay"
                    android:layout_width="match_parent"
                    android:layout_height="44dp"
                    android:layout_marginTop="6dp"
                    android:backgroundTint="#2563EB"
                    android:text="2. Grant Floating Overlay Permission"
                    android:textColor="#FFFFFF"
                    android:textSize="11sp" />

                <Button
                    android:id="@+id/btn_start_floating_hud"
                    android:layout_width="match_parent"
                    android:layout_height="50dp"
                    android:layout_marginTop="16dp"
                    android:backgroundTint="#10B981"
                    android:text="🚀 LAUNCH DRAGON FARM HUD"
                    android:textColor="#FFFFFF"
                    android:textSize="13sp"
                    android:textStyle="bold" />

                <!-- AI Self-Improvement Memory Stats Card -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:background="#131C2E"
                    android:orientation="vertical"
                    android:padding="12dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="🧠 AI Self-Improving Memory"
                        android:textColor="#38BDF8"
                        android:textSize="13sp"
                        android:textStyle="bold" />

                    <TextView
                        android:id="@+id/tv_memory_stats"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="Total Raids Learned: 0 | Optimal Angle: BOTTOM_LEFT\nWin Rate: 100% | Auto Key Rotation: ACTIVE"
                        android:textColor="#94A3B8"
                        android:textSize="11sp" />
                </LinearLayout>
            </LinearLayout>

            <!-- TAB 2 CONTENT: AI BRAIN & API KEYS -->
            <LinearLayout
                android:id="@+id/tab_content_ai"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:visibility="gone">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="🧠 AI Provider &amp; Key Rotator"
                    android:textColor="#38BDF8"
                    android:textSize="18sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="Add free API keys (Gemini, Groq, OpenRouter). Keys auto-rotate if a 429 rate limit is reached!"
                    android:textColor="#94A3B8"
                    android:textSize="11sp" />

                <!-- Provider Selector -->
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:text="Select AI Provider:"
                    android:textColor="#F8FAFC"
                    android:textSize="12sp"
                    android:textStyle="bold" />

                <Spinner
                    android:id="@+id/spinner_provider"
                    android:layout_width="match_parent"
                    android:layout_height="44dp"
                    android:layout_marginTop="4dp"
                    android:background="#1E293B" />

                <!-- API Key Input + Add Button -->
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:text="Add API Key (Free tier supported):"
                    android:textColor="#F8FAFC"
                    android:textSize="12sp"
                    android:textStyle="bold" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:layout_marginTop="4dp">

                    <EditText
                        android:id="@+id/et_api_key_input"
                        android:layout_width="0dp"
                        android:layout_height="44dp"
                        android:layout_weight="1"
                        android:background="#1E293B"
                        android:hint="Paste API Key here..."
                        android:textColor="#F8FAFC"
                        android:textColorHint="#64748B"
                        android:paddingHorizontal="10dp"
                        android:textSize="12sp"
                        android:inputType="textPassword" />

                    <Button
                        android:id="@+id/btn_add_api_key"
                        android:layout_width="wrap_content"
                        android:layout_height="44dp"
                        android:layout_marginStart="6dp"
                        android:backgroundTint="#10B981"
                        android:text="+ ADD"
                        android:textColor="#FFFFFF"
                        android:textSize="11sp"
                        android:textStyle="bold" />
                </LinearLayout>

                <!-- Active Keys Pool List -->
                <TextView
                    android:id="@+id/tv_active_keys_count"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="10dp"
                    android:text="API Key Pool: 0 Key(s) Loaded (Auto-Rotation Enabled)"
                    android:textColor="#10B981"
                    android:textSize="11sp" />

                <!-- Live Model Discovery -->
                <Button
                    android:id="@+id/btn_fetch_models"
                    android:layout_width="match_parent"
                    android:layout_height="42dp"
                    android:layout_marginTop="14dp"
                    android:backgroundTint="#6366F1"
                    android:text="🔄 FETCH LIVE MODELS FROM PROVIDER"
                    android:textColor="#FFFFFF"
                    android:textSize="11sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Selected Vision AI Model:"
                    android:textColor="#F8FAFC"
                    android:textSize="12sp"
                    android:textStyle="bold" />

                <Spinner
                    android:id="@+id/spinner_live_models"
                    android:layout_width="match_parent"
                    android:layout_height="44dp"
                    android:layout_marginTop="4dp"
                    android:background="#1E293B" />

            </LinearLayout>

        </LinearLayout>
    </ScrollView>
</LinearLayout>
"""

with open('android_app/app/src/main/res/layout/activity_main.xml', 'w') as f:
    f.write(activity_main_xml)

# 2. Update MainActivity.kt to wire up tabs, key rotator, model fetcher, and memory
main_activity_kt = """package com.cocai.autoclicker.ui

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
        tvMemoryStats.text = "Total Raids Learned: ${stats.optInt("total_raids", 0)} | Optimal Attack Angle: $bestSide\\nWin Rate: ${stats.optString("win_rate", "100%")} | Auto Key Rotation: ACTIVE"
    }
}
"""

with open('android_app/app/src/main/java/com/cocai/autoclicker/ui/MainActivity.kt', 'w') as f:
    f.write(main_activity_kt)

print("Updated activity_main.xml and MainActivity.kt with Dual Tabs & Key Rotator.")
