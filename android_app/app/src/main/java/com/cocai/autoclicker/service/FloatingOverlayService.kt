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
import com.cocai.autoclicker.R
import com.cocai.autoclicker.engine.CocFarmingEngine
import com.cocai.autoclicker.engine.CocStrategy

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var farmingEngine: CocFarmingEngine? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val accService = AutoClickAccessibilityService.instance
        if (accService != null) {
            farmingEngine = CocFarmingEngine(accService)
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

        val btnToggle = floatingView?.findViewById<Button>(R.id.btn_toggle_play)
        val btnStrategy = floatingView?.findViewById<Button>(R.id.btn_strategy)
        val tvStatus = floatingView?.findViewById<TextView>(R.id.tv_hud_status)

        var selectedStrategy = CocStrategy.ZAP_DRAGON_FARMING

        btnStrategy?.setOnClickListener {
            selectedStrategy = when (selectedStrategy) {
                CocStrategy.ZAP_DRAGON_FARMING -> CocStrategy.ELECTRO_DRAGON_SPAM
                CocStrategy.ELECTRO_DRAGON_SPAM -> CocStrategy.DRAGON_RIDER_SMASH
                CocStrategy.DRAGON_RIDER_SMASH -> CocStrategy.SNEAKY_GOBLIN_ORE_FARM
                CocStrategy.SNEAKY_GOBLIN_ORE_FARM -> CocStrategy.ZAP_DRAGON_FARMING
            }
            btnStrategy.text = selectedStrategy.name.replace("_", " ")
        }

        btnToggle?.setOnClickListener {
            val engine = farmingEngine
            if (engine != null) {
                if (engine.isRunning) {
                    engine.stopEngine()
                    btnToggle.text = "▶ START"
                    tvStatus?.text = "[IDLE]"
                } else {
                    engine.startEngine(selectedStrategy)
                    btnToggle.text = "⏸ PAUSE"
                    tvStatus?.text = "[FARMING]"
                }
            } else {
                tvStatus?.text = "[ENABLE ACCESSIBILITY]"
            }
        }

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
        if (floatingView != null) {
            windowManager.removeView(floatingView)
        }
    }
}
