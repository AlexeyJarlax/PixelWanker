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
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pavlovalexey.pavlovAlexeySandbox.R

class PixelWankerOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var gridOverlayView: FrameLayout? = null
    private var controlsOverlayView: FrameLayout? = null
    private var gridView: GridView? = null
    private var isGridVisible = true
    private var config: GridConfig = GridConfig.default()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val density = resources.displayMetrics.density
        config = GridConfig.fromIntent(intent, density)
        gridView?.update(config.spacingPx, config.lineColorArgb)
        if (controlsOverlayView == null) {
            gridOverlayView = createGridOverlayView(config)
            controlsOverlayView = createControlsOverlayView()

            val gridParams = WindowManager.LayoutParams(
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

            windowManager?.addView(gridOverlayView, gridParams)
            windowManager?.addView(controlsOverlayView, createControlsLayoutParams())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        gridOverlayView?.let { windowManager?.removeView(it) }
        controlsOverlayView?.let { windowManager?.removeView(it) }
        gridOverlayView = null
        controlsOverlayView = null
        gridView = null
        windowManager = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createGridOverlayView(config: GridConfig): FrameLayout {
        val root = FrameLayout(this)

        val view = GridView(
            context = this,
            spacingPx = config.spacingPx,
            lineColorArgb = config.lineColorArgb
        )
        gridView = view

        root.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        return root
    }

    private fun createControlsOverlayView(): FrameLayout {
        val root = FrameLayout(this)
        val density = resources.displayMetrics.density
        fun dpToPx(value: Int): Int = (value * density).toInt()

        fun createControlButton(iconRes: Int): ImageView = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@PixelWankerOverlayService, iconRes))
            setColorFilter(Color.WHITE)

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.argb(200, 0, 0, 0))
            }
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val hintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_hide_grid)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            maxLines = 3
            maxWidth = dpToPx(260)

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.argb(200, 0, 0, 0))
            }
            setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8))
            alpha = 0f
            visibility = View.VISIBLE
        }

        val backButton = createControlButton(android.R.drawable.ic_menu_revert).apply {
            setOnClickListener { openAppHomeAndCloseOverlay() }
            contentDescription = getString(R.string.overlay_back)
        }

        val toggleButton = createControlButton(
            if (isGridVisible) R.drawable.grid_30dp else R.drawable.grid_off_30dp
        )

        val closeButton = createControlButton(android.R.drawable.ic_menu_close_clear_cancel).apply {
            setOnClickListener { stopSelf() }
            contentDescription = getString(R.string.overlay_close)
        }

        fun updateToggleIcon() {
            val iconRes = if (isGridVisible) R.drawable.grid_30dp else R.drawable.grid_off_30dp
            toggleButton.setImageDrawable(ContextCompat.getDrawable(this@PixelWankerOverlayService, iconRes))
            toggleButton.contentDescription = getString(
                if (isGridVisible) R.string.overlay_hide_grid else R.string.overlay_show_grid
            )
        }

        toggleButton.setOnClickListener {
            hintTextView.visibility = View.GONE

            isGridVisible = !isGridVisible
            if (isGridVisible) showGridOverlay() else hideGridOverlay()
            updateToggleIcon()
        }

        val controlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val buttonSize = dpToPx(38)
        val buttonSpacing = dpToPx(4)

        controlsContainer.addView(
            backButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize).apply { marginEnd = buttonSpacing }
        )
        controlsContainer.addView(
            toggleButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize).apply { marginEnd = buttonSpacing }
        )
        controlsContainer.addView(
            closeButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize)
        )

        val controlsRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        controlsRoot.addView(controlsContainer)

        controlsRoot.addView(
            hintTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dpToPx(8) }
        )

        val controlsParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        root.addView(controlsRoot, controlsParams)

        hintTextView.animate().alpha(1f).setDuration(250).start()
        hintTextView.postDelayed({
            hintTextView.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction { hintTextView.visibility = View.GONE }
                .start()
        }, 4000)

        return root
    }

    private fun openAppHomeAndCloseOverlay() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(launchIntent)
        }
        stopSelf()
    }

    private fun showGridOverlay() {
        if (gridOverlayView != null) return

        val view = createGridOverlayView(config)

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

        gridOverlayView = view
        windowManager?.addView(view, params)
        controlsOverlayView?.let { controlsView ->
            windowManager?.removeView(controlsView)
            windowManager?.addView(controlsView, createControlsLayoutParams())
        }
    }

    private fun hideGridOverlay() {
        gridOverlayView?.let { windowManager?.removeView(it) }
        gridOverlayView = null
        gridView = null
    }

    private fun createControlsLayoutParams(): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

    private class GridView(
        context: Context,
        spacingPx: Float,
        lineColorArgb: Int,
    ) : View(context) {

        private var spacing: Float = spacingPx

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColorArgb
            strokeWidth = 1f
        }

        fun update(spacingPx: Float, lineColorArgb: Int) {
            spacing = spacingPx
            paint.color = lineColorArgb
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val step = spacing.coerceAtLeast(2f)

            var x = 0f
            while (x <= width) {
                canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                x += step
            }

            var y = 0f
            while (y <= height) {
                canvas.drawLine(0f, y, width.toFloat(), y, paint)
                y += step
            }
        }
    }

    private data class GridConfig(
        val spacingPx: Float,
        val lineColorArgb: Int,
    ) {
        companion object {
            private const val EXTRA_CELL_VALUE = "extra_cell_value"
            private const val EXTRA_CELL_UNIT = "extra_cell_unit"
            private const val EXTRA_COLOR = "extra_color"

            fun default(): GridConfig {
                return GridConfig(
                    spacingPx = 20f,
                    lineColorArgb = applyAlpha(Color.BLACK, 80)
                )
            }

            fun fromIntent(intent: Intent?, density: Float): GridConfig {
                if (intent == null) return default()

                val value = intent.getIntExtra(EXTRA_CELL_VALUE, 20)
                val unit = intent.getStringExtra(EXTRA_CELL_UNIT) ?: "px"
                val baseColor = intent.getIntExtra(EXTRA_COLOR, Color.BLACK)

                val spacingPx = if (unit == "dp") value * density else value.toFloat()
                return GridConfig(
                    spacingPx = spacingPx,
                    lineColorArgb = applyAlpha(baseColor, 80)
                )
            }

            private fun applyAlpha(color: Int, alpha: Int): Int {
                return (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)
            }

            fun buildStartIntent(
                context: Context,
                cellValue: Int,
                unit: String,
                baseColor: Int
            ): Intent {
                return Intent(context, PixelWankerOverlayService::class.java).apply {
                    putExtra(EXTRA_CELL_VALUE, cellValue)
                    putExtra(EXTRA_CELL_UNIT, unit)
                    putExtra(EXTRA_COLOR, baseColor)
                }
            }
        }
    }

    companion object {
        fun start(
            context: Context,
            cellValue: Int,
            unit: String,
            baseColor: Int
        ) {
            val intent = GridConfig.buildStartIntent(context, cellValue, unit, baseColor)
            context.startService(intent)
        }
    }
}
