package com.cocai.autoclicker.service

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.ClashAutomationCore
import kotlin.math.abs

/**
 * 👑 Macrorify-Style Play Mode Floating HUD Service
 *
 * - Compact 42dp draggable bubble with edge snapping
 * - Expandable horizontal 40dp Play Mode pill toolbar
 * - State and position persistence via SharedPreferences
 */
class FloatingHUDService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingRootView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var automationCore: ClashAutomationCore? = null
    private lateinit var prefs: SharedPreferences

    private var isExpanded: Boolean = true
    private var screenWidth: Int = 1080
    private var screenHeight: Int = 2400

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("macrorify_hud_prefs", Context.MODE_PRIVATE)

        fetchScreenDimensions()
        initCore()
        setupFloatingLayout()
    }

    private fun fetchScreenDimensions() {
        val windowMgr = windowManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowMgr.currentWindowMetrics
            screenWidth = metrics.bounds.width()
            screenHeight = metrics.bounds.height()
        } else {
            @Suppress("DEPRECATION")
            val display = windowMgr.defaultDisplay
            val size = Point()
            @Suppress("DEPRECATION")
            display.getSize(size)
            screenWidth = size.x
            screenHeight = size.y
        }
    }

    private fun initCore() {
        val acc = AutoClickAccessibilityService.instance
        if (acc != null) {
            automationCore = ClashAutomationCore(this, acc)

            automationCore?.onStatusUpdate = { newStatus ->
                floatingRootView?.post {
                    val tvStatus = floatingRootView?.findViewById<TextView>(R.id.tv_hud_status_badge)
                    val shortStatus = when {
                        newStatus.contains("STARTING", true) -> "START"
                        newStatus.contains("BUILDER", true) || newStatus.contains("WALL", true) -> "WALLS"
                        newStatus.contains("QUICK TRAIN", true) || newStatus.contains("ARMY", true) -> "TRAIN"
                        newStatus.contains("SEARCHING", true) || newStatus.contains("ATTACK", true) -> "SEARCH"
                        newStatus.contains("WAVE", true) || newStatus.contains("RAIDING", true) -> "RAID"
                        newStatus.contains("VICTORY", true) -> "WIN"
                        newStatus.contains("COOLDOWN", true) || newStatus.contains("PAUSE", true) -> "REST"
                        else -> "ACTIVE"
                    }
                    tvStatus?.text = shortStatus
                }
            }

            acc.emergencyStopListener = {
                stopMacroExecution()
                Toast.makeText(this, "🚨 [PANIC STOP ACTIVATED] Macro Halted via Volume Down!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stopMacroExecution() {
        automationCore?.stopLoop()
        val tvStatus = floatingRootView?.findViewById<TextView>(R.id.tv_hud_status_badge)
        val btnPlayPause = floatingRootView?.findViewById<Button>(R.id.btn_hud_play_pause)
        tvStatus?.text = "IDLE"
        btnPlayPause?.text = "▶"
    }

    private fun setupFloatingLayout() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Restore saved position
        val savedX = prefs.getInt("pos_x", screenWidth - 250)
        val savedY = prefs.getInt("pos_y", 200)
        isExpanded = prefs.getBoolean("is_expanded", true)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = savedX
        params.y = savedY
        windowParams = params

        floatingRootView = LayoutInflater.from(this).inflate(R.layout.layout_macrorify_hud, null)
        windowManager?.addView(floatingRootView, params)

        val llBubble = floatingRootView?.findViewById<LinearLayout>(R.id.ll_minimized_bubble)
        val llToolbar = floatingRootView?.findViewById<LinearLayout>(R.id.ll_expanded_toolbar)
        val btnCollapse = floatingRootView?.findViewById<Button>(R.id.btn_hud_collapse)
        val btnPlayPause = floatingRootView?.findViewById<Button>(R.id.btn_hud_play_pause)
        val btnStop = floatingRootView?.findViewById<Button>(R.id.btn_hud_stop)
        val btnSettings = floatingRootView?.findViewById<Button>(R.id.btn_hud_settings)
        val btnExit = floatingRootView?.findViewById<Button>(R.id.btn_hud_exit)
        val tvStatus = floatingRootView?.findViewById<TextView>(R.id.tv_hud_status_badge)

        updateExpandedState(isExpanded)

        // 1. Minimized Bubble Click -> Expand
        llBubble?.setOnClickListener {
            updateExpandedState(true)
        }

        // 2. Collapse Button Click -> Minimize to Bubble
        btnCollapse?.setOnClickListener {
            updateExpandedState(false)
        }

        // 3. Play / Pause Macro
        btnPlayPause?.setOnClickListener {
            if (automationCore == null) initCore()
            val core = automationCore
            if (core == null) {
                Toast.makeText(this, "Enable Accessibility Service first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!core.isRunning) {
                core.startLoop(accounts = 2)
                btnPlayPause.text = "⏸"
                tvStatus?.text = "FARM"
                Toast.makeText(this, "🚀 Ari AI Play Mode Started!", Toast.LENGTH_SHORT).show()
            } else {
                core.stopLoop()
                btnPlayPause.text = "▶"
                tvStatus?.text = "PAUSED"
                Toast.makeText(this, "⏸ Ari AI Paused", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Stop Macro
        btnStop?.setOnClickListener {
            stopMacroExecution()
            Toast.makeText(this, "⏹ Farm Loops Reset & Stopped", Toast.LENGTH_SHORT).show()
        }

        // 5. Quick Settings Dialog Toggle
        btnSettings?.setOnClickListener {
            val core = automationCore
            val batt = core?.watchdog?.batteryPercentage ?: 100
            val temp = core?.watchdog?.currentTemperatureCelsius ?: 30.0f
            val raids = core?.totalRaidsCompleted ?: 0
            val accIdx = (core?.accountSwitcher?.currentAccountIndex ?: 0) + 1
            Toast.makeText(this, "⚙ [MACRORIFY HUD] Acc #$accIdx | ⚔️ Raids: $raids | 🔋 $batt% | 🌡️ ${temp}°C", Toast.LENGTH_LONG).show()
        }

        // 6. Exit Button
        btnExit?.setOnClickListener {
            stopSelf()
        }

        // Touch & Drag Handling with Edge Snapping
        setupTouchAndDragGesture()
    }

    private fun updateExpandedState(expanded: Boolean) {
        isExpanded = expanded
        prefs.edit().putBoolean("is_expanded", isExpanded).apply()

        val llBubble = floatingRootView?.findViewById<LinearLayout>(R.id.ll_minimized_bubble)
        val llToolbar = floatingRootView?.findViewById<LinearLayout>(R.id.ll_expanded_toolbar)

        if (expanded) {
            llBubble?.visibility = View.GONE
            llToolbar?.visibility = View.VISIBLE
        } else {
            llToolbar?.visibility = View.GONE
            llBubble?.visibility = View.VISIBLE
        }
    }

    private fun setupTouchAndDragGesture() {
        floatingRootView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                val params = windowParams ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return false // Allow child clicks if no drag occurs
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        if (abs(dx) > 12 || abs(dy) > 12) {
                            isDragging = true
                            params.x = (initialX + dx).toInt().coerceIn(0, screenWidth - 100)
                            params.y = (initialY + dy).toInt().coerceIn(0, screenHeight - 100)
                            windowManager?.updateViewLayout(floatingRootView, params)
                            return true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isDragging) {
                            // Snap to closest edge (Left or Right)
                            snapToClosestEdge(params.x)
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun snapToClosestEdge(currentX: Int) {
        val params = windowParams ?: return
        val targetX = if (currentX + 150 < screenWidth / 2) 20 else (screenWidth - (floatingRootView?.width ?: 200) - 20)

        val animator = ValueAnimator.ofInt(params.x, targetX)
        animator.duration = 250L
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            params.x = animation.animatedValue as Int
            try {
                windowManager?.updateViewLayout(floatingRootView, params)
            } catch (e: Exception) {
                // View detached safely
            }
        }
        animator.start()

        // Save position
        prefs.edit().putInt("pos_x", targetX).putInt("pos_y", params.y).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        automationCore?.stopLoop()
        if (floatingRootView != null) {
            try {
                windowManager?.removeView(floatingRootView)
            } catch (e: Exception) {
                // Already detached
            }
        }
    }
}
