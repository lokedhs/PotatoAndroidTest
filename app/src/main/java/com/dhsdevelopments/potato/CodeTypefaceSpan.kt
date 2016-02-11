package com.dhsdevelopments.potato

import android.graphics.*
import android.text.style.ReplacementSpan

class CodeTypefaceSpan : ReplacementSpan() {
    private val typeface = Typeface.MONOSPACE

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt): Int {
        val bounds = Rect()
        paint.typeface = typeface
        paint.getTextBounds(text.toString(), start, end, bounds)
        return bounds.right + FRAME_MARGIN * 2
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        paint.typeface = typeface

        val bounds = Rect()
        paint.getTextBounds(text.toString(), start, end, bounds)

        val metrics = Paint.FontMetrics()
        paint.getFontMetrics(metrics)

        val oldColour = paint.color

        paint.color = Color.rgb(120, 120, 120)
        val drawX1 = x
        val drawY1 = y + metrics.top
        val drawX2 = x + bounds.right.toFloat() + (FRAME_MARGIN * 2).toFloat()
        val drawY2 = y + metrics.bottom
        canvas.drawRect(drawX1, drawY1, drawX2, drawY2, paint)

        paint.color = Color.rgb(210, 210, 210)
        canvas.drawRect(drawX1 + FRAME_WIDTH,
                drawY1 + FRAME_WIDTH,
                drawX2 - FRAME_WIDTH,
                drawY2 - FRAME_WIDTH,
                paint)

        paint.color = oldColour
        canvas.drawText(text, start, end, x + FRAME_MARGIN, y.toFloat(), paint)
    }

    companion object {
        private val FRAME_MARGIN = 3
        private val FRAME_WIDTH = 1
    }
}
