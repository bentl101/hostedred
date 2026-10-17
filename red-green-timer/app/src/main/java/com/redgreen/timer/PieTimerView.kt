package com.redgreen.timer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * A big disc that shows how much time is left, like a "Time Timer".
 * While running, a white wedge shrinks clockwise as time passes.
 * When finished, it shows a big white check mark instead.
 */
class PieTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /** 1.0 = full time left, 0.0 = no time left. */
    var fractionLeft: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var finished: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val wedgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val oval = RectF()
    private val checkPath = Path()

    override fun onDraw(canvas: Canvas) {
        val size = min(width - paddingLeft - paddingRight, height - paddingTop - paddingBottom).toFloat()
        val cx = paddingLeft + (width - paddingLeft - paddingRight) / 2f
        val cy = paddingTop + (height - paddingTop - paddingBottom) / 2f
        val r = size / 2f
        oval.set(cx - r, cy - r, cx + r, cy + r)

        canvas.drawOval(oval, trackPaint)

        if (finished) {
            checkPaint.strokeWidth = r * 0.16f
            checkPath.reset()
            checkPath.moveTo(cx - r * 0.45f, cy + r * 0.02f)
            checkPath.lineTo(cx - r * 0.12f, cy + r * 0.35f)
            checkPath.lineTo(cx + r * 0.50f, cy - r * 0.32f)
            canvas.drawPath(checkPath, checkPaint)
        } else if (fractionLeft > 0f) {
            // Start at 12 o'clock; the wedge disappears clockwise as time runs out.
            val sweep = 360f * fractionLeft
            canvas.drawArc(oval, -90f + (360f - sweep), sweep, true, wedgePaint)
        }
    }
}
