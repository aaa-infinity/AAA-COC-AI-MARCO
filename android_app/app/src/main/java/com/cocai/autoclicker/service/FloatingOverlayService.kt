package com.cocai.autoclicker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.AriAiAgent
import com.cocai.autoclicker.engine.CocStrategy

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var ariAgent: AriAiAgent? = null
    private var selectedStrategy = CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        initAgent()
        setupFloatingLayout()
    }

    private fun initAgent() {
        val acc = AutoClickAccessibilityService.instance
        if (acc != null) {
            ariAgent = AriAiAgent(this, acc)

            // Wire Hardware Volume Down Panic Stop
            acc.emergencyStopListener = {
                stopMacroExecution()
                Toast.makeText(this, "🚨 [PANIC STOP ACTIVATED] Ari AI Agent Halted via Volume Down Key!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopMacroExecution() {
        ariAgent?.stopAgent()
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_floating_status)
        val btnPlayPause = floatingView?.findViewById<Button>(R.id.btn_floating_play_pause)
        tvStatus?.text = "[PANIC STOPPED] Idle"
        btnPlayPause?.text = "▶ START"
    }

    private fun setupFloatingLayout() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 80
        params.y = 150

        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_controller, null)
        windowManager?.addView(floatingView, params)

        val btnPlayPause = floatingView?.findViewById<Button>(R.id.btn_floating_play_pause)
        val btnClean = floatingView?.findViewById<Button>(R.id.btn_floating_clean)
        val btnStrategy = floatingView?.findViewById<Button>(R.id.btn_floating_strategy)
        val btnConsole = floatingView?.findViewById<Button>(R.id.btn_floating_console)
        val btnClose = floatingView?.findViewById<Button>(R.id.btn_floating_close)
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_floating_status)

        // 1. Play / Pause Button (Hermes Ari Agent)
        btnPlayPause?.setOnClickListener {
            if (ariAgent == null) {
                initAgent()
            }

            val agent = ariAgent
            if (agent == null) {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!agent.isAgentActive) {
                agent.startAgent(selectedStrategy)
                btnPlayPause.text = "⏸ PAUSE"
                tvStatus?.text = "🏛️ [ARI ACTIVE] ${selectedStrategy.name.replace("_", " ")}"
                Toast.makeText(this, "🚀 Ari AI Agent (Hermes Class) Started: ${selectedStrategy.name}", Toast.LENGTH_SHORT).show()
            } else {
                agent.stopAgent()
                btnPlayPause.text = "▶ START"
                tvStatus?.text = "[PAUSED] Idle"
                Toast.makeText(this, "⏸ Ari AI Agent Paused", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Clean Base Obstacles / Gem Box
        btnClean?.setOnClickListener {
            val agent = ariAgent
            if (agent == null) {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            } else {
                agent.supervisor.cleanBaseObstacles {
                    Toast.makeText(this, "Base Obstacles & Gem Boxes Cleaned!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 3. Macro Console Button
        btnConsole?.setOnClickListener {
            val raids = ariAgent?.totalRaids ?: 0
            Toast.makeText(this, "🏛️ Ari Agent: Raids: $raids | VolDown: ARMED | Hermes Loop: ACTIVE", Toast.LENGTH_SHORT).show()
        }

        // 4. Close / Exit Button
        btnClose?.setOnClickListener {
            stopSelf()
        }

        // 5. Strategy Selector
        btnStrategy?.setOnClickListener {
            selectedStrategy = when (selectedStrategy) {
                CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH -> CocStrategy.ZAP_DRAGON_FARMING
                CocStrategy.ZAP_DRAGON_FARMING -> CocStrategy.ELECTRO_DRAGON_SPAM
                CocStrategy.ELECTRO_DRAGON_SPAM -> CocStrategy.DRAGON_RIDER_SMASH
                CocStrategy.DRAGON_RIDER_SMASH -> CocStrategy.SNEAKY_GOBLIN_ORE_FARM
                CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH
                else -> CocStrategy.ROOT_RIDER_OVERGROWTH_SMASH
            }
            btnStrategy.text = selectedStrategy.name.take(8)
            tvStatus?.text = "[IDLE] ${selectedStrategy.name.replace("_", " ")}"
            Toast.makeText(this, "Selected Strategy: ${selectedStrategy.name}", Toast.LENGTH_SHORT).show()
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
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        ariAgent?.stopAgent()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
    }
}
