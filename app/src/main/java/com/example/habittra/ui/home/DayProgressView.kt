package com.example.habittra.ui.home

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class DayProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var dayNumber: String = ""
        set(v) { field = v; invalidate() }

    var progress: Float = 0f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }

    var isDaySelected: Boolean = false
        set(v) { field = v; invalidate() }

    var isToday: Boolean = false
        set(v) { field = v; invalidate() }

    var ringColor: Int = Color.WHITE
        set(v) { field = v; invalidate() }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val arcRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx      = width / 2f
        val cy      = height / 2f
        val strokeW = width * 0.09f
        val radius  = (width / 2f) - strokeW

        // Background fill
        bgPaint.color = when {
            isDaySelected && isToday -> Color.WHITE
            isDaySelected            -> Color.parseColor("#333333")
            else                     -> Color.TRANSPARENT
        }
        canvas.drawCircle(cx, cy, radius - strokeW / 2, bgPaint)

        // Dim track ring
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
        trackPaint.strokeWidth = strokeW
        trackPaint.color = Color.parseColor("#333333")
        canvas.drawOval(arcRect, trackPaint)

        // Colored progress arc
        if (progress > 0f) {
            progressPaint.color = ringColor
            progressPaint.strokeWidth = strokeW
            canvas.drawArc(arcRect, -90f, 360f * progress, false, progressPaint)
        }

        // Selected outline when not today
        if (isDaySelected && !isToday) {
            trackPaint.color = Color.parseColor("#AAAAAA")
            trackPaint.strokeWidth = strokeW * 0.6f
            canvas.drawOval(arcRect, trackPaint)
        }

        // Day number text
        textPaint.textSize = width * 0.32f
        textPaint.color = if (isDaySelected) Color.BLACK else Color.WHITE
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(dayNumber, cx, textY, textPaint)
    }
}