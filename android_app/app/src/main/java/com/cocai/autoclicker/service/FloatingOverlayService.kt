package com.cocai.autoclicker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.AntiAfkPatrolEngine
import com.cocai.autoclicker.engine.AutoDonateEngine
import com.cocai.autoclicker.engine.AutonomousSupervisor
import com.cocai.autoclicker.engine.CocFarmingEngine
import com.cocai.autoclicker.engine.CocStrategy

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var farmingEngine: CocFarmingEngine? = null
    private var donateEngine: AutoDonateEngine? = null
    private var antiAfkEngine: AntiAfkPatrolEngine? = null
    private var supervisor: AutonomousSupervisor? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val accService = AutoClickAccessibilityService.instance
        if (accService != null) {
            farmingEngine = CocFarmingEngine(accService)
            donateEngine = AutoDonateEngine(accService)
            antiAfkEngine = AntiAfkPatrolEngine(accService)
            supervisor = AutonomousSupervisor(this, accService)
            supervisor?.startSupervisor()
        }

        setupFloatingView()
    }

    private fun setupFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_hud, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 180
        }

        windowManager.addView(floatingView, params)

        val btnVision = floatingView?.findViewById<Button>(R.id.btn_hud_vision)
        val btnToggle = floatingView?.findViewById<Button>(R.id.btn_toggle_play)
        val btnSettings = floatingView?.findViewById<Button>(R.id.btn_hud_settings)
        val btnTools = floatingView?.findViewById<Button>(R.id.btn_hud_tools)
        val btnConsole = floatingView?.findViewById<Button>(R.id.btn_hud_console)
        val btnClose = floatingView?.findViewById<Button>(R.id.btn_hud_close)
        val btnStrategy = floatingView?.findViewById<Button>(R.id.btn_strategy)
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)

        var selectedStrategy = CocStrategy.ZAP_DRAGON_FARMING

        // Wire Emergency Panic Stop Listener from Hardware Volume Down Key
        AutoClickAccessibilityService.instance?.emergencyStopListener = {
            farmingEngine?.stopEngine()
            antiAfkEngine?.stopPatrol()
            btnToggle?.text = "▶"
            btnToggle?.setBackgroundColor(0xFF10B981.toInt())
            tvStatus?.text = "[PANIC STOPPED]"
        }

        // 1. Vision Snap / Target Selection
        btnVision?.setOnClickListener {
            Toast.makeText(this, "📸 Vision AI: Multi-Touch Screenshot-to-Code triggered!", Toast.LENGTH_SHORT).show()
            tvStatus?.text = "[VISION] 4-Finger Wave Ready"
        }

        // 2. Play / Pause Button
        btnToggle?.setOnClickListener {
            val engine = farmingEngine
            if (engine != null) {
                if (engine.isRunning) {
                    engine.stopEngine()
                    btnToggle.text = "▶"
                    btnToggle.setBackgroundColor(0xFF10B981.toInt())
                    tvStatus?.text = "[IDLE] ${selectedStrategy.name.replace("_", " ")}"
                } else {
                    engine.startEngine(selectedStrategy)
                    btnToggle.text = "⏸"
                    btnToggle.setBackgroundColor(0xFFF59E0B.toInt())
                    tvStatus?.text = "[4-FINGER MT] ${selectedStrategy.name.replace("_", " ")}"
                }
            } else {
                tvStatus?.text = "[ERR: ACCESSIBILITY]"
                Toast.makeText(this, "Accessibility Service not connected!", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Settings Button
        btnSettings?.setOnClickListener {
            Toast.makeText(this, "⚙️ Panic Key: VOL DOWN | Multi-Touch: 4-FINGER | Min Gold/Elixir: 500k", Toast.LENGTH_LONG).show()
        }

        // 4. Tools: Auto Donate, CC Request & Anti-AFK
        btnTools?.setOnClickListener {
            val donate = donateEngine
            val sup = supervisor
            if (donate != null && !donate.isDonating) {
                Toast.makeText(this, "🛠️ Running Clan Chat Auto-Donate & CC Request...", Toast.LENGTH_SHORT).show()
                tvStatus?.text = "[TOOLS] Donating Troops"
                donate.startAutoDonate {
                    sup?.performClanCastleRequest {
                        tvStatus?.text = "[IDLE] Clan Routines Complete"
                    }
                }
            } else {
                sup?.cleanBaseObstacles {
                    Toast.makeText(this, "Base Obstacles & Gem Boxes Cleaned!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 5. Macro Console Button
        btnConsole?.setOnClickListener {
            val raids = farmingEngine?.raidsCompleted ?: 0
            Toast.makeText(this, "💻 Console: Raids: $raids | VolDown Stop: READY | Multi-Touch: ON", Toast.LENGTH_SHORT).show()
        }

        // 6. Close / Exit Button
        btnClose?.setOnClickListener {
            stopSelf()
        }

        // Strategy Selector
        btnStrategy?.setOnClickListener {
            selectedStrategy = when (selectedStrategy) {
                CocStrategy.ZAP_DRAGON_FARMING -> CocStrategy.ELECTRO_DRAGON_SPAM
                CocStrategy.ELECTRO_DRAGON_SPAM -> CocStrategy.DRAGON_RIDER_SMASH
                CocStrategy.DRAGON_RIDER_SMASH -> CocStrategy.SNEAKY_GOBLIN_ORE_FARM
                CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> CocStrategy.ZAP_DRAGON_FARMING
            }
            btnStrategy.text = selectedStrategy.name.take(8)
            tvStatus?.text = "[IDLE] ${selectedStrategy.name.replace("_", " ")}"
            Toast.makeText(this, "Selected: ${selectedStrategy.name}", Toast.LENGTH_SHORT).show()
        }

        // Dragging gesture
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        farmingEngine?.stopEngine()
        antiAfkEngine?.stopPatrol()
        supervisor?.stopSupervisor()
        if (floatingView != null) {
            windowManager.removeView(floatingView)
        }
    }
}
