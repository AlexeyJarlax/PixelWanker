package com.pavlovalexey.pavlovAlexeySandbox.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class PixelWankerOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = createOverlayView()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { view ->
            windowManager?.removeView(view)
        }
        overlayView = null
        windowManager = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createOverlayView(): FrameLayout {
        val root = FrameLayout(this)

        val density = resources.displayMetrics.density
        fun dpToPx(value: Int): Int = (value * density).toInt()

        val gridLayer = FrameLayout(this)
        val gridView = GridView(this)
        gridLayer.addView(
            gridView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        root.addView(
            gridLayer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        fun createControlButton(): ImageView = ImageView(this).apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    this@PixelWankerOverlayService,
                    android.R.drawable.ic_menu_close_clear_cancel
                )
            )
            setColorFilter(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.argb(200, 0, 0, 0))
            }
            setPadding(
                dpToPx(4),
                dpToPx(4),
                dpToPx(4),
                dpToPx(4)
            )
        }

        val toggleButton = createControlButton()
        val closeButton = createControlButton().apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    this@PixelWankerOverlayService,
                    android.R.drawable.ic_menu_close_clear_cancel
                )
            )
            setOnClickListener { stopSelf() }
        }

        var isGridVisible = true
        fun updateToggleIcon() {
            val iconRes = if (isGridVisible) {
                android.R.drawable.presence_invisible
            } else {
                android.R.drawable.presence_visible
            }
            toggleButton.setImageDrawable(
                ContextCompat.getDrawable(this@PixelWankerOverlayService, iconRes)
            )
            toggleButton.contentDescription = if (isGridVisible) {
                "Скрыть сетку"
            } else {
                "Показать сетку"
            }
        }

        updateToggleIcon()
        toggleButton.setOnClickListener {
            isGridVisible = !isGridVisible
            gridLayer.visibility = if (isGridVisible) View.VISIBLE else View.GONE
            updateToggleIcon()
        }

        val controlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val buttonSize = dpToPx(25)
        val buttonSpacing = dpToPx(6)

        controlsContainer.addView(
            toggleButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize).apply {
                marginEnd = buttonSpacing
            }
        )
        controlsContainer.addView(
            closeButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize)
        )

        val controlsParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = dpToPx(12)
            marginEnd = dpToPx(12)
        }
        root.addView(controlsContainer, controlsParams)

        return root
    }

    private class GridView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255)
            strokeWidth = 1f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val spacing = 20f
            var x = 0f
            while (x <= width) {
                canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                x += spacing
            }

            var y = 0f
            while (y <= height) {
                canvas.drawLine(0f, y, width.toFloat(), y, paint)
                y += spacing
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PixelWankerOverlayService::class.java)
            context.startService(intent)
        }
    }
}
