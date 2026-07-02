package com.example.habittra.ui.home

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class HeatmapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var habitColor: Int = Color.YELLOW
    var doneDates: Set<String> = emptySet()
        set(value) { field = value; invalidate() }
    var allDates: List<String> = emptyList()
        set(value) { field = value; requestLayout(); invalidate() }

    private val cols = 53
    private val rows = 7
    private val dotSize = 28f   // fixed size in px — big enough to see clearly
    private val dotGap  = 5f
    private val cornerR = dotSize * 0.28f

    // Total canvas width = 53 weeks * (dotSize + gap)
    private fun totalWidth()  = (cols * (dotSize + dotGap)).toInt()
    private fun totalHeight() = (rows * (dotSize + dotGap)).toInt()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Fixed size — scroll view handles the horizontal overflow
        setMeasuredDimension(totalWidth(), totalHeight())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (allDates.isEmpty()) return

        val doneDim = Color.argb(255,
            (Color.red(habitColor)   * 0.20f).toInt(),
            (Color.green(habitColor) * 0.20f).toInt(),
            (Color.blue(habitColor)  * 0.20f).toInt()
        )

        allDates.forEachIndexed { index, date ->
            val col  = index / 7
            val row  = index % 7
            val left = col * (dotSize + dotGap)
            val top  = row * (dotSize + dotGap)
            val rect = RectF(left, top, left + dotSize, top + dotSize)
            paint.color = if (date in doneDates) habitColor else doneDim
            canvas.drawRoundRect(rect, cornerR, cornerR, paint)
        }
    }
}