package com.pavlovalexey.pavlovAlexeySandbox.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pavlovalexey.pavlovAlexeySandbox.R
import android.widget.Toast
import java.util.Locale
import kotlin.math.abs

class PixelWankerOverlayService : Service() {

    private val availableGridSizes = listOf(4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40, 60, 100, 200, 300, 400)

    private var windowManager: WindowManager? = null
    private var gridOverlayView: FrameLayout? = null
    private var controlsOverlayView: FrameLayout? = null
    private var gridView: GridView? = null
    private var isGridVisible = true
    private var config: GridConfig = GridConfig.default()
    private val dragHandler = Handler(Looper.getMainLooper())
    private val touchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }
    private var overlayTranslationX = 0f
    private var overlayTranslationY = 0f
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var dragInitialTranslationX = 0f
    private var dragInitialTranslationY = 0f
    private var isDraggingOverlay = false
    private var pendingLongPress: Runnable? = null
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
        val root = DragOverlayLayout(this)
        root.translationX = overlayTranslationX
        root.translationY = overlayTranslationY

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
        val root = DragOverlayLayout(this)
        root.translationX = overlayTranslationX
        root.translationY = overlayTranslationY
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

        /*** кнопка смещения сетки влево ***/
        val goLeftButton = createControlButton(R.drawable.ic_icon_arrow_left_30dp).apply {
            setOnClickListener { shiftGridBy(-1f, 0f) }
            contentDescription = getString(R.string.overlay_shift_left)
        }

        /*** подсказка: сместить сетку влево ***/
        val goLeftHintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_shift_left)
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

        /*** кнопка смещения сетки вниз ***/
        val shiftDownButton = createControlButton(android.R.drawable.arrow_down_float).apply {
            setOnClickListener { shiftGridBy(0f, 1f) }
            contentDescription = getString(R.string.overlay_shift_down)
        }

        /*** подсказка: сместить сетку вниз ***/
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

        /*** кнопка возврата в главное меню приложения ***/
        val goBackButton = createControlButton(android.R.drawable.ic_menu_revert).apply {
            setOnClickListener { openAppHomeAndCloseOverlay() }
            contentDescription = getString(R.string.overlay_back)
        }

        /*** подсказка: вернуться в меню приложения ***/
        val goBackHintTextView = TextView(this).apply {
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

        /*** подсказка: изменить размер ячейки сетки ***/
        val sizeHintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_change_grid_size)
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

        /*** кнопка показа/скрытия сетки ***/
        val toggleButton = createControlButton(
            if (isGridVisible) R.drawable.grid_30dp else R.drawable.grid_off_30dp
        )

        /*** подсказка: скрыть/показать сетку ***/
        val gridHintTextView = TextView(this).apply {
            text = getString(R.string.overlay_hint_hide_grid)
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

        val hintContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(
                gridHintTextView,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        var isHintVisible = true

        /*** кнопка показа/скрытия всех подсказок ***/
        val hintToggleButton = createControlButton(android.R.drawable.ic_dialog_info).apply {
            contentDescription = getString(R.string.overlay_toggle_hint)
        }

        val extraHintViews = listOf(goLeftHintTextView, goBackHintTextView, shiftDownHintTextView, sizeHintTextView)
        val allHintViews = listOf(gridHintTextView) + extraHintViews
        var updateControlsSizing: ((Boolean) -> Unit)? = null

        fun updateHintVisibility(visible: Boolean) {
            isHintVisible = visible
            hintContainer.visibility = if (visible) View.VISIBLE else View.GONE
            extraHintViews.forEach { hintView ->
                hintView.visibility = if (visible) View.VISIBLE else View.GONE
            }
            hintToggleButton.setColorFilter(if (visible) Color.YELLOW else Color.WHITE)
            updateControlsSizing?.invoke(visible)
        }

        /*** кнопка-индикатор размера ячейки (переключение размеров по нажатию) ***/
        val gridInfoView = TextView(this).apply {
            text = "${config.cellValue}\n${config.unit}"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            gravity = Gravity.CENTER
            setLines(2)
            background = createControlBackground()
        }

        fun updateGridInfo() {
            gridInfoView.text = "${config.cellValue}\n${config.unit}"
        }

        fun stripAlpha(color: Int): Int = color and 0x00FFFFFF

        fun cycleGridDimension() {
            val unitVariants = listOf("px", "dp")
            val currentSizeIndex = availableGridSizes.indexOf(config.cellValue).takeIf { it >= 0 } ?: 0
            val currentUnitIndex = unitVariants.indexOf(config.unit).takeIf { it >= 0 } ?: 0
            val isLastSize = currentSizeIndex == availableGridSizes.lastIndex

            val nextSize = if (isLastSize) availableGridSizes.first() else availableGridSizes[currentSizeIndex + 1]
            val nextUnit = if (isLastSize) unitVariants[(currentUnitIndex + 1) % unitVariants.size] else unitVariants[currentUnitIndex]
            val nextSpacingPx = if (nextUnit == "dp") nextSize * density else nextSize.toFloat()

            config = config.copy(
                spacingPx = nextSpacingPx,
                cellValue = nextSize,
                unit = nextUnit
            )
            gridView?.update(config.spacingPx, config.lineColorArgb, config.extraLineColorArgb)
            updateGridInfo()

            GridSettingsStore.save(
                context = this,
                settings = GridUserSettings(
                    cellValue = config.cellValue,
                    unit = config.unit,
                    baseColor = stripAlpha(config.lineColorArgb),
                    extraColor = config.extraLineColorArgb?.let(::stripAlpha)
                )
            )
        }

        gridInfoView.setOnClickListener {
            updateHintVisibility(false)
            cycleGridDimension()
        }

        /*** кнопка закрытия окна с сеткой ***/
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
                hintContainer.postDelayed(dismissHint, 30_000)
            }
        }

        hintToggleButton.setOnClickListener {
            updateHintVisibility(!isHintVisible)
            scheduleHintAutoHide()
        }

        val controlsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val buttonSize = dpToPx(38)
        val buttonSizeExtra = dpToPx(56)
        val buttonSpacing = dpToPx(4)

        val column0 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        column0.addView(
            goLeftHintTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = buttonSpacing }
        )

        val column0Params = LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = buttonSpacing
        }
        controlsContainer.addView(column0, column0Params)

        val column1 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val goLeftButtonParams = LinearLayout.LayoutParams(buttonSize, buttonSize)
        column1.addView(goLeftButton, goLeftButtonParams)

        val column1Params = LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = buttonSpacing
        }
        controlsContainer.addView(column1, column1Params)

        val column2 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        column2.addView(
            goBackHintTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = buttonSpacing }
        )
        val goBackButtonParams = LinearLayout.LayoutParams(buttonSize, buttonSize).apply { bottomMargin = buttonSpacing }
        column2.addView(goBackButton, goBackButtonParams)
        val shiftDownButtonParams = LinearLayout.LayoutParams(buttonSize, buttonSize).apply { bottomMargin = buttonSpacing }
        column2.addView(shiftDownButton, shiftDownButtonParams)
        column2.addView(
            shiftDownHintTextView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val column2Params = LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = buttonSpacing
        }
        controlsContainer.addView(column2, column2Params)

        val column3 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val hintToggleButtonParams = LinearLayout.LayoutParams(buttonSize, buttonSize).apply { topMargin = buttonSpacing }
        column3.addView(hintToggleButton, hintToggleButtonParams)

        val column3Params = LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = buttonSpacing
        }
        controlsContainer.addView(column3, column3Params)

        val column4 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val hintContainerParams = LinearLayout.LayoutParams(
            buttonSizeExtra,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = buttonSpacing }
        column4.addView(hintContainer, hintContainerParams)
        val toggleButtonParams = LinearLayout.LayoutParams(buttonSizeExtra, buttonSize)
        column4.addView(toggleButton, toggleButtonParams)
        val gridInfoViewParams = LinearLayout.LayoutParams(buttonSizeExtra, buttonSize).apply { topMargin = buttonSpacing }
        column4.addView(gridInfoView, gridInfoViewParams)
        column4.addView(
            sizeHintTextView,
            LinearLayout.LayoutParams(
                buttonSizeExtra,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = buttonSpacing }
        )

        val column4Params = LinearLayout.LayoutParams(buttonSizeExtra, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = buttonSpacing
        }
        controlsContainer.addView(column4, column4Params)

        val column5 = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val closeButtonParams = LinearLayout.LayoutParams(buttonSize, buttonSize)
        column5.addView(closeButton, closeButtonParams)

        val column5Params = LinearLayout.LayoutParams(buttonSize, LinearLayout.LayoutParams.WRAP_CONTENT)
        controlsContainer.addView(column5, column5Params)

        updateControlsSizing = { isExpanded ->
            val target = if (isExpanded) buttonSizeExtra else buttonSize

            listOf(column0Params, column1Params, column2Params, column3Params, column4Params, column5Params).forEach { params ->
                params.width = target
            }
            listOf(
                goLeftButtonParams,
                goBackButtonParams,
                shiftDownButtonParams,
                hintToggleButtonParams,
                toggleButtonParams,
                gridInfoViewParams,
                closeButtonParams
            ).forEach { params ->
                params.width = target
                params.height = target
            }
            allHintViews.forEach { hintView ->
                (hintView.layoutParams as? LinearLayout.LayoutParams)?.width = target
            }
            hintContainerParams.width = target

            controlsContainer.requestLayout()
        }

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
        allHintViews.forEach { hintView ->
            hintView.setOnClickListener { hideHint() }
        }
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
        view.translationX = overlayTranslationX
        view.translationY = overlayTranslationY
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

    private fun updateOverlayTranslation(x: Float, y: Float) {
        overlayTranslationX = x
        overlayTranslationY = y
        gridOverlayView?.translationX = x
        gridOverlayView?.translationY = y
        controlsOverlayView?.translationX = x
        controlsOverlayView?.translationY = y
    }

    private fun scheduleOverlayDragStart(rawX: Float, rawY: Float, view: View) {
        cancelOverlayDrag()
        dragStartX = rawX
        dragStartY = rawY
        dragInitialTranslationX = overlayTranslationX
        dragInitialTranslationY = overlayTranslationY
        pendingLongPress = Runnable {
            isDraggingOverlay = true
            view.parent?.requestDisallowInterceptTouchEvent(true)
        }
        dragHandler.postDelayed(pendingLongPress!!, 1_000L)
    }

    private fun cancelOverlayDrag() {
        pendingLongPress?.let { dragHandler.removeCallbacks(it) }
        pendingLongPress = null
        isDraggingOverlay = false
    }

    private inner class DragOverlayLayout(context: Context) : FrameLayout(context) {
        private var downX = 0f
        private var downY = 0f

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = ev.rawX
                    downY = ev.rawY
                    scheduleOverlayDragStart(ev.rawX, ev.rawY, this)
                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isDraggingOverlay) {
                        val dx = ev.rawX - downX
                        val dy = ev.rawY - downY
                        if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                            cancelOverlayDrag()
                        }
                        return false
                    }
                    return true
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    cancelOverlayDrag()
                    return false
                }
            }
            return super.onInterceptTouchEvent(ev)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    if (isDraggingOverlay) {
                        val dx = event.rawX - dragStartX
                        val dy = event.rawY - dragStartY
                        updateOverlayTranslation(
                            dragInitialTranslationX + dx,
                            dragInitialTranslationY + dy
                        )
                        return true
                    }
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    cancelOverlayDrag()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
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
