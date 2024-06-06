package com.example.sharenote

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.Log
import kotlin.math.log

class BorderDrawable(private val borderColor: Int) : Drawable() {
    private val paint = Paint().apply {
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    override fun draw(canvas: Canvas) {
        Log.e("BorderDrawable", "draw")
        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}