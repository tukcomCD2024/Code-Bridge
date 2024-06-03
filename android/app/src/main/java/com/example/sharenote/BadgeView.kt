package com.example.sharenote

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BadgeView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var count: Int = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    fun setCount(count: Int) {
        this.count = count
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (count > 0) {
            val radius = 30f
            val cx = width - radius
            val cy = radius

            // Draw red circle
            canvas.drawCircle(cx, cy, radius, paint)

            // Draw count
            val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(count.toString(), cx, textY, textPaint)
        }
    }
}
