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
import android.widget.Toast
import java.util.Locale

class PixelWankerOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var gridOverlayView: FrameLayout? = null
    private var controlsOverlayView: FrameLayout? = null
    private var gridView: GridView? = null
    private var isGridVisible = true
    private var config: GridConfig = GridConfig.default()
    private val density: Float
        get() = resources.displayMetrics.density

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val density = resources.displayMetrics.density
        config = GridConfig.fromIntent(intent, density)
        val dm = resources.displayMetrics
        val toastText = String.format(
            Locale.US,
            "widthPx=%d | density=%.2f | stepPx=%.1f",
            dm.widthPixels,
            dm.density,
            config.spacingPx
        )
        Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
        gridView?.update(config.spacingPx, config.lineColorArgb, config.extraLineColorArgb)

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
            lineColorArgb = config.lineColorArgb,
            extraLineColorArgb = config.extraLineColorArgb
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
        fun dpToPx(value: Int): Int = (value * density).toInt()

        fun createControlBackground(): GradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(8).toFloat()
            setColor(Color.argb(200, 0, 0, 0))
        }

        fun createControlButton(iconRes: Int): ImageView = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@PixelWankerOverlayService, iconRes))
            setColorFilter(Color.WHITE)

            background = createControlBackground()
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val hintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_hide_grid)
            setTextColor(Color.RED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            maxLines = 18
            maxWidth = dpToPx(200)
            gravity = Gravity.CENTER

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.LTGRAY)
            }
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }

        val backButton = createControlButton(android.R.drawable.ic_menu_revert).apply {
            setOnClickListener { openAppHomeAndCloseOverlay() }
            contentDescription = getString(R.string.overlay_back)
        }

        val backHintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_back_to_menu)
            setTextColor(Color.RED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            maxLines = 10
            maxWidth = dpToPx(180)
            gravity = Gravity.CENTER

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.LTGRAY)
            }
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }

        val shiftDownButton = createControlButton(android.R.drawable.arrow_down_float).apply {
            setOnClickListener { shiftGridBy(0f, 1f) }
            contentDescription = getString(R.string.overlay_shift_down)
        }

        val shiftDownHintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_shift_down)
            setTextColor(Color.RED)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            maxLines = 10
            maxWidth = dpToPx(180)
            gravity = Gravity.CENTER

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.LTGRAY)
            }
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
        }

        val toggleButton = createControlButton(
            if (isGridVisible) R.drawable.grid_30dp else R.drawable.grid_off_30dp
        )

        val hintContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                hintTextView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        var isHintVisible = true

        val hintToggleButton = createControlButton(android.R.drawable.ic_dialog_info).apply {
            contentDescription = getString(R.string.overlay_toggle_hint)
        }

        val extraHintViews = listOf(backHintTextView, shiftDownHintTextView)

        fun updateHintVisibility(visible: Boolean) {
            isHintVisible = visible
            hintContainer.visibility = if (visible) View.VISIBLE else View.GONE
            extraHintViews.forEach { hintView ->
                hintView.visibility = if (visible) View.VISIBLE else View.GONE
            }
            hintToggleButton.setColorFilter(if (visible) Color.YELLOW else Color.WHITE)
        }

        val gridInfoView = TextView(this).apply {
            text = "${config.cellValue}\n${config.unit}"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            gravity = Gravity.CENTER
            setLines(2)
            background = createControlBackground()
        }

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
            updateHintVisibility(false)
            isGridVisible = !isGridVisible
            if (isGridVisible) showGridOverlay() else hideGridOverlay()
            updateToggleIcon()
        }

        val dismissHint = Runnable {
            updateHintVisibility(false)
        }

        fun scheduleHintAutoHide() {
            hintContainer.removeCallbacks(dismissHint)
            if (isHintVisible) {
                hintContainer.postDelayed(dismissHint, 8_000)
            }
        }

        hintToggleButton.setOnClickListener {
            updateHintVisibility(true)
            scheduleHintAutoHide()
        }

        val controlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val buttonSize = dpToPx(38)
        val buttonSizeExtra = dpToPx(48)
        val buttonSpacing = dpToPx(4)

        val backColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        backColumn.addView(
            backHintTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = buttonSpacing }
        )
        backColumn.addView(
            backButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize)
        )
        backColumn.addView(
            shiftDownButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize).apply { topMargin = buttonSpacing }
        )
        backColumn.addView(
            shiftDownHintTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = buttonSpacing }
        )

        controlsContainer.addView(
            backColumn,
            LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = buttonSpacing
            }
        )
        controlsContainer.addView(
            hintToggleButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize).apply { marginEnd = buttonSpacing }
        )
        val gridColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        gridColumn.addView(
            hintContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = buttonSpacing }
        )

        gridColumn.addView(
            toggleButton,
            LinearLayout.LayoutParams(buttonSize, buttonSize)
        )
        gridColumn.addView(
            gridInfoView,
            LinearLayout.LayoutParams(buttonSize, buttonSize).apply { topMargin = buttonSpacing }
        )

        controlsContainer.addView(
            gridColumn,
            LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = buttonSpacing
            }
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

        val controlsParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        root.addView(controlsRoot, controlsParams)

        fun hideHint() {
            updateHintVisibility(false)
            scheduleHintAutoHide()
        }

        hintContainer.setOnClickListener { hideHint() }
        hintTextView.setOnClickListener { hideHint() }
        updateHintVisibility(true)
        scheduleHintAutoHide()

        return root
    }

    private fun shiftGridBy(deltaXUnits: Float, deltaYUnits: Float) {
        val shiftPx = density
        gridView?.shiftBy(deltaXUnits * shiftPx, deltaYUnits * shiftPx)
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
        extraLineColorArgb: Int?,
    ) : View(context) {

        private var spacing: Float = spacingPx
        private var offsetX: Float = 0f
        private var offsetY: Float = 0f

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lineColorArgb
            strokeWidth = 1f
        }

        private val extraPaint: Paint? = extraLineColorArgb?.let { c ->
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = c
                strokeWidth = 1f
            }
        }

        fun update(spacingPx: Float, lineColorArgb: Int, extraLineColorArgb: Int?) {
            spacing = spacingPx
            paint.color = lineColorArgb
            _extraColor = extraLineColorArgb
            invalidate()
        }

        fun shiftBy(deltaX: Float, deltaY: Float) {
            val step = spacing.coerceAtLeast(2f)
            offsetX = normalizeOffset(offsetX + deltaX, step)
            offsetY = normalizeOffset(offsetY + deltaY, step)
            invalidate()
        }

        private var _extraColor: Int? = extraLineColorArgb

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val step = spacing.coerceAtLeast(2f)
            val extraColor = _extraColor
            val extra = if (extraColor != null) {
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = extraColor
                    strokeWidth = 1f
                }
            } else {
                null
            }

            val startX = offsetX - step
            var x = startX
            while (x <= width) {
                if (extra == null) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                } else {
                    canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                    canvas.drawLine(x + 1f, 0f, x + 1f, height.toFloat(), extra)
                }
                x += step
            }

            val startY = offsetY - step
            var y = startY
            while (y <= height) {
                if (extra == null) {
                    canvas.drawLine(0f, y, width.toFloat(), y, paint)
                } else {
                    canvas.drawLine(0f, y, width.toFloat(), y, paint)
                    canvas.drawLine(0f, y + 1f, width.toFloat(), y + 1f, extra)
                }
                y += step
            }
        }

        private fun normalizeOffset(value: Float, step: Float): Float {
            val remainder = value % step
            return if (remainder < 0f) remainder + step else remainder
        }
    }

    private data class GridConfig(
        val spacingPx: Float,
        val lineColorArgb: Int,
        val extraLineColorArgb: Int?,
        val cellValue: Int,
        val unit: String,
    ) {
        companion object {
            private const val EXTRA_CELL_VALUE = "extra_cell_value"
            private const val EXTRA_CELL_UNIT = "extra_cell_unit"
            private const val EXTRA_COLOR = "extra_color"
            private const val EXTRA_HAS_EXTRA_COLOR = "extra_has_extra_color"
            private const val EXTRA_EXTRA_COLOR = "extra_extra_color"

            fun default(): GridConfig {
                return GridConfig(
                    spacingPx = 20f,
                    lineColorArgb = applyAlpha(Color.BLACK, 80),
                    extraLineColorArgb = null,
                    cellValue = 20,
                    unit = "px"
                )
            }

            fun fromIntent(intent: Intent?, density: Float): GridConfig {
                if (intent == null) return default()

                val value = intent.getIntExtra(EXTRA_CELL_VALUE, 20)
                val unit = intent.getStringExtra(EXTRA_CELL_UNIT) ?: "px"
                val baseColor = intent.getIntExtra(EXTRA_COLOR, Color.BLACK)

                val hasExtra = intent.getBooleanExtra(EXTRA_HAS_EXTRA_COLOR, false)
                val extraColor = if (hasExtra) intent.getIntExtra(EXTRA_EXTRA_COLOR, Color.YELLOW) else null

                val spacingPx = if (unit == "dp") value * density else value.toFloat()
                return GridConfig(
                    spacingPx = spacingPx,
                    lineColorArgb = applyAlpha(baseColor, 80),
                    extraLineColorArgb = extraColor?.let { applyAlpha(it, 80) },
                    cellValue = value,
                    unit = unit
                )
            }

            private fun applyAlpha(color: Int, alpha: Int): Int {
                return (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)
            }

            fun buildStartIntent(
                context: Context,
                cellValue: Int,
                unit: String,
                baseColor: Int,
                extraColor: Int?,
            ): Intent {
                return Intent(context, PixelWankerOverlayService::class.java).apply {
                    putExtra(EXTRA_CELL_VALUE, cellValue)
                    putExtra(EXTRA_CELL_UNIT, unit)
                    putExtra(EXTRA_COLOR, baseColor)

                    putExtra(EXTRA_HAS_EXTRA_COLOR, extraColor != null)
                    if (extraColor != null) {
                        putExtra(EXTRA_EXTRA_COLOR, extraColor)
                    }
                }
            }
        }
    }

    companion object {
        fun start(
            context: Context,
            cellValue: Int,
            unit: String,
            baseColor: Int,
            extraColor: Int?,
        ) {
            val intent = GridConfig.buildStartIntent(context, cellValue, unit, baseColor, extraColor)
            context.startService(intent)
        }
    }
}
