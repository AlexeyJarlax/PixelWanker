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
import androidx.core.content.ContextCompat

class PixelWankerOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var gridOverlayView: FrameLayout? = null
    private var closeOverlayView: ImageView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        gridOverlayView = createGridOverlayView()
        closeOverlayView = createCloseOverlayView()

        val gridParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val closeSize = 20
        val closeParams = WindowManager.LayoutParams(
            closeSize,
            closeSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        gridOverlayView?.let { windowManager?.addView(it, gridParams) }
        closeOverlayView?.let { windowManager?.addView(it, closeParams) }
    }

    override fun onDestroy() {
        super.onDestroy()
        gridOverlayView?.let { view ->
            windowManager?.removeView(view)
        }
        closeOverlayView?.let { view ->
            windowManager?.removeView(view)
        }
        gridOverlayView = null
        closeOverlayView = null
        windowManager = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createGridOverlayView(): FrameLayout {
        val root = FrameLayout(this)

        val gridView = GridView(this)
        root.addView(
            gridView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        return root
    }

    private fun createCloseOverlayView(): ImageView =
        ImageView(this).apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    this@PixelWankerOverlayService,
                    android.R.drawable.ic_menu_close_clear_cancel
                )
            )
            setColorFilter(Color.WHITE)
            setOnClickListener { stopSelf() }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6f
                setColor(Color.argb(200, 0, 0, 0))
            }
            setPadding(4, 4, 4, 4)
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
