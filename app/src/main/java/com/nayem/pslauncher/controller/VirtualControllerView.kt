package com.nayem.pslauncher.controller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Virtual on-screen controller — joystick (left) + ABXY (right).
 * Drawn with Canvas only (no images) to keep the APK tiny.
 * v1 reports touches via onButton() — the Remote Play / game layer in v2
 * will turn these into real key events. For Android games, the pad is
 * visual feedback + a test target; physical pads control games directly.
 */
class VirtualControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var onButton: ((String) -> Unit)? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 255, 255, 255)
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 42f; textAlign = Paint.Align.CENTER
    }
    private var stickX = 0f; private var stickY = 0f
    private var stickActive = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        // Left joystick base + knob
        val cx = w * 0.20f; val cy = h * 0.55f; val r = 110f
        canvas.drawCircle(cx, cy, r, paint)
        canvas.drawCircle(cx + stickX, cy + stickY, 48f,
            Paint(paint).apply { color = Color.argb(160, 255, 255, 255) })
        // Right buttons: △ □ ○ ✕
        val bx = w * 0.82f; val by = h * 0.55f
        canvas.drawCircle(bx, by - 95, 52f, paint)
        canvas.drawCircle(bx - 95, by, 52f, paint)
        canvas.drawCircle(bx + 95, by, 52f, paint)
        canvas.drawCircle(bx, by + 95, 52f, paint)
        canvas.drawText("△", bx, by - 80, textPaint)
        canvas.drawText("□", bx - 95, by + 15, textPaint)
        canvas.drawText("○", bx + 95, by + 15, textPaint)
        canvas.drawText("✕", bx, by + 110, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x; val y = event.y
        val w = width.toFloat(); val h = height.toFloat()
        if (x < w * 0.4f) {
            // Joystick zone
            val cx = w * 0.20f; val cy = h * 0.55f
            stickX = (x - cx).coerceIn(-70f, 70f)
            stickY = (y - cy).coerceIn(-70f, 70f)
            stickActive = event.action != MotionEvent.ACTION_UP
            if (!stickActive) { stickX = 0f; stickY = 0f }
            onButton?.invoke("stick %.2f, %.2f".format(stickX / 70f, stickY / 70f))
            invalidate()
            return true
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            val bx = w * 0.82f; val by = h * 0.55f
            val btn = when {
                dist(x, y, bx, by - 95) < 60 -> "△"
                dist(x, y, bx - 95, by) < 60 -> "□"
                dist(x, y, bx + 95, by) < 60 -> "○"
                dist(x, y, bx, by + 95) < 60 -> "✕"
                else -> null
            }
            btn?.let { onButton?.invoke("button $it") }
        }
        return true
    }

    private fun dist(x: Float, y: Float, cx: Float, cy: Float): Float {
        val dx = x - cx; val dy = y - cy
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
