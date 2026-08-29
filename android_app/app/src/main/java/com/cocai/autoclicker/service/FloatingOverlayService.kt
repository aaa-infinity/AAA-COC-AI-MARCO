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
import com.cocai.autoclicker.engine.ClashAutomationCore

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var automationCore: ClashAutomationCore? = null
    private var accountsCount = 2

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        initCore()
        setupFloatingLayout()
    }

    private fun initCore() {
        val acc = AutoClickAccessibilityService.instance
        if (acc != null) {
            automationCore = ClashAutomationCore(this, acc)

            // Live status updater
            automationCore?.onStatusUpdate = { newStatus ->
                floatingView?.post {
                    val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)
                    tvStatus?.text = newStatus
                }
            }

            // Hardware Volume Down Panic Stop
            acc.emergencyStopListener = {
                stopMacroExecution()
                Toast.makeText(this, "🚨 [PANIC STOP ACTIVATED] AI Macro Halted via Volume Down!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopMacroExecution() {
        automationCore?.stopLoop()
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)
        val btnPlayPause = floatingView?.findViewById<Button>(R.id.btn_toggle_play)
        tvStatus?.text = "⏸ [PANIC STOPPED] Idle"
        btnPlayPause?.text = "▶"
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

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_hud, null)
        windowManager?.addView(floatingView, params)

        val btnPlayPause = floatingView?.findViewById<Button>(R.id.btn_toggle_play)
        val btnVision = floatingView?.findViewById<Button>(R.id.btn_hud_vision)
        val btnTools = floatingView?.findViewById<Button>(R.id.btn_hud_tools)
        val btnStrategy = floatingView?.findViewById<Button>(R.id.btn_strategy)
        val btnConsole = floatingView?.findViewById<Button>(R.id.btn_hud_console)
        val btnClose = floatingView?.findViewById<Button>(R.id.btn_hud_close)
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)

        // 1. Play / Pause Button
        btnPlayPause?.setOnClickListener {
            if (automationCore == null) {
                initCore()
            }

            val core = automationCore
            if (core == null) {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!core.isRunning) {
                Toast.makeText(this, "🚀 AI Starting! Multi-Account ($accountsCount Accs) & Anti-Ban active.", Toast.LENGTH_LONG).show()
                core.startLoop(accountsCount)
                btnPlayPause.text = "⏸"
                tvStatus?.text = "🚀 [ACTIVE] Camera Reset..."
            } else {
                core.stopLoop()
                btnPlayPause.text = "▶"
                tvStatus?.text = "⏸ [PAUSED] Idle"
                Toast.makeText(this, "⏸ AI Macro Paused", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Tools Button: Instant Wall Dump
        btnTools?.setOnClickListener {
            val acc = AutoClickAccessibilityService.instance
            if (acc != null) {
                acc.performPercentageTap(0.500f, 0.500f) {
                    Toast.makeText(this, "🎯 Screen Calibrated! Center Tap Dispatched.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Vision Button: Switch Supercell ID Account Now
        btnVision?.setOnClickListener {
            val core = automationCore
            if (core != null) {
                core.accountSwitcher.switchToNextAccount(accountsCount) {
                    Toast.makeText(this, "🔄 Switched to Supercell ID Account #${core.accountSwitcher.currentAccountIndex + 1}!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Macro Console Info Button: Displays Battery, Temp, Raids & Account
        btnConsole?.setOnClickListener {
            val core = automationCore
            val temp = core?.watchdog?.currentTemperatureCelsius ?: 30.0f
            val batt = core?.watchdog?.batteryPercentage ?: 100
            val raids = core?.totalRaidsCompleted ?: 0
            val accIdx = (core?.accountSwitcher?.currentAccountIndex ?: 0) + 1
            Toast.makeText(this, "🔋 Battery: $batt% | 🌡️ Temp: ${temp}°C | ⚔️ Raids: $raids | 🔄 Acc: #$accIdx", Toast.LENGTH_LONG).show()
        }

        // 5. Close Button
        btnClose?.setOnClickListener {
            stopSelf()
        }

        // 6. Strategy / Account Switcher Count Button (Cycles 1, 2, 3, 4 accounts)
        btnStrategy?.setOnClickListener {
            accountsCount = when (accountsCount) {
                1 -> 2
                2 -> 3
                3 -> 4
                else -> 1
            }
            btnStrategy.text = "${accountsCount} Accs"
            Toast.makeText(this, "Multi-Account Auto-Cycle set to $accountsCount Accounts!", Toast.LENGTH_SHORT).show()
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
        automationCore?.stopLoop()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
    }
}
