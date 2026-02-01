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
import com.pavlovalexey.pavlovAlexeySandbox.R

class PixelWankerOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var gridOverlayView: FrameLayout? = null
    private var controlsOverlayView: FrameLayout? = null
    private var isGridVisible = true
    private var gridSpacingPx = DEFAULT_GRID_SIZE.toFloat()
    private var gridColor = applyAlpha(GridColor.WHITE.argb)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        applySettingsFromIntent(intent)

        if (isGridVisible) {
            gridOverlayView?.let { view ->
                windowManager?.removeView(view)
            }
            gridOverlayView = createGridOverlayView()
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
        }

        if (controlsOverlayView == null) {
            controlsOverlayView = createControlsOverlayView()
            val controlsParams = createControlsLayoutParams()
            windowManager?.addView(controlsOverlayView, controlsParams)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        gridOverlayView?.let { view ->
            windowManager?.removeView(view)
        }
        controlsOverlayView?.let { view ->
            windowManager?.removeView(view)
        }
        gridOverlayView = null
        controlsOverlayView = null
        windowManager = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun applySettingsFromIntent(intent: Intent?) {
        val size = intent?.getIntExtra(EXTRA_GRID_SIZE, DEFAULT_GRID_SIZE) ?: DEFAULT_GRID_SIZE
        val unitName = intent?.getStringExtra(EXTRA_GRID_UNIT) ?: GridUnit.PX.name
        val unit = GridUnit.values().firstOrNull { it.name == unitName } ?: GridUnit.PX
        val colorName = intent?.getStringExtra(EXTRA_GRID_COLOR) ?: GridColor.WHITE.name
        val color = GridColor.values().firstOrNull { it.name == colorName } ?: GridColor.WHITE
        val density = resources.displayMetrics.density
        gridSpacingPx = if (unit == GridUnit.DP) size * density else size.toFloat()
        gridColor = applyAlpha(color.argb)
    }

    private fun createGridOverlayView(): FrameLayout {
        val root = FrameLayout(this)

        val gridView = GridView(this, gridSpacingPx, gridColor)
        root.addView(
            gridView,
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

        fun updateToggleIcon() {
            val iconRes = if (isGridVisible) {
                R.drawable.grid_off_30dp
            } else {
                R.drawable.grid_30dp
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
            if (isGridVisible) {
                showGridOverlay()
            } else {
                hideGridOverlay()
            }
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
            gravity = Gravity.CENTER
        }
        root.addView(controlsContainer, controlsParams)

        return root
    }

    private fun showGridOverlay() {
        if (gridOverlayView != null) {
            return
        }
        val view = createGridOverlayView()
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
        gridOverlayView?.let { view ->
            windowManager?.removeView(view)
        }
        gridOverlayView = null
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
        private val spacingPx: Float,
        color: Int
    ) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = 1f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            var x = 0f
            while (x <= width) {
                canvas.drawLine(x, 0f, x, height.toFloat(), paint)
                x += spacingPx
            }

            var y = 0f
            while (y <= height) {
                canvas.drawLine(0f, y, width.toFloat(), y, paint)
                y += spacingPx
            }
        }
    }

    companion object {
        const val DEFAULT_GRID_SIZE = 20
        private const val EXTRA_GRID_SIZE = "extra_grid_size"
        private const val EXTRA_GRID_UNIT = "extra_grid_unit"
        private const val EXTRA_GRID_COLOR = "extra_grid_color"
        private const val GRID_ALPHA = 120
        private const val PREFS_NAME = "pixel_wanker_prefs"
        private const val PREF_GRID_SIZE = "pref_grid_size"
        private const val PREF_GRID_UNIT = "pref_grid_unit"
        private const val PREF_GRID_COLOR = "pref_grid_color"

        enum class GridUnit {
            PX,
            DP
        }

        enum class GridColor(val argb: Int) {
            BLACK(Color.BLACK),
            WHITE(Color.WHITE),
            RED(Color.RED)
        }

        data class GridSettings(
            val size: Int,
            val unit: GridUnit,
            val color: GridColor
        )

        fun start(
            context: Context,
            gridSize: Int = DEFAULT_GRID_SIZE,
            gridUnit: GridUnit = GridUnit.PX,
            gridColor: GridColor = GridColor.WHITE
        ) {
            val intent = Intent(context, PixelWankerOverlayService::class.java)
                .putExtra(EXTRA_GRID_SIZE, gridSize)
                .putExtra(EXTRA_GRID_UNIT, gridUnit.name)
                .putExtra(EXTRA_GRID_COLOR, gridColor.name)
            context.startService(intent)
        }

        fun startWithSavedSettings(context: Context) {
            val settings = loadGridSettings(context)
            start(
                context = context,
                gridSize = settings.size,
                gridUnit = settings.unit,
                gridColor = settings.color
            )
        }

        fun saveGridSettings(
            context: Context,
            gridSize: Int,
            gridUnit: GridUnit,
            gridColor: GridColor
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(PREF_GRID_SIZE, gridSize)
                .putString(PREF_GRID_UNIT, gridUnit.name)
                .putString(PREF_GRID_COLOR, gridColor.name)
                .apply()
        }

        fun loadGridSettings(context: Context): GridSettings {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val size = prefs.getInt(PREF_GRID_SIZE, DEFAULT_GRID_SIZE)
            val unitName = prefs.getString(PREF_GRID_UNIT, GridUnit.PX.name) ?: GridUnit.PX.name
            val colorName =
                prefs.getString(PREF_GRID_COLOR, GridColor.WHITE.name) ?: GridColor.WHITE.name
            val unit = GridUnit.values().firstOrNull { it.name == unitName } ?: GridUnit.PX
            val color = GridColor.values().firstOrNull { it.name == colorName } ?: GridColor.WHITE
            return GridSettings(size, unit, color)
        }

        private fun applyAlpha(color: Int): Int =
            Color.argb(
                GRID_ALPHA,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
    }
}
