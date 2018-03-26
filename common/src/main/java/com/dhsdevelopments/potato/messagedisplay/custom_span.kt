package com.dhsdevelopments.potato.messagedisplay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.style.LineBackgroundSpan
import android.text.style.ReplacementSpan
import com.dhsdevelopments.potato.common.R

/**
 * Span used to render inline code format. I.e. text that is wrapped in backquotes.
 */
class CodeTypefaceSpan(val context: Context) : ReplacementSpan() {
    private val typeface = Typeface.MONOSPACE
    private val frameColour = ContextCompat.getColor(context, R.color.code_inline_frame)
    private val backgroundColour = ContextCompat.getColor(context, R.color.code_background)

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val bounds = Rect()
        paint.typeface = typeface
        paint.getTextBounds(text.toString(), start, end, bounds)
        return bounds.right + FRAME_MARGIN * 2
    }

    @Suppress("UnnecessaryVariable")
    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        paint.typeface = typeface

        val bounds = Rect()
        paint.getTextBounds(text.toString(), start, end, bounds)

        val metrics = Paint.FontMetrics()
        paint.getFontMetrics(metrics)

        val oldColour = paint.color

        paint.color = frameColour
        val drawX1 = x
        val drawY1 = y + metrics.top
        val drawX2 = x + bounds.right.toFloat() + (FRAME_MARGIN * 2).toFloat()
        val drawY2 = y + metrics.bottom
        canvas.drawRect(drawX1, drawY1, drawX2, drawY2, paint)

        paint.color = backgroundColour
        canvas.drawRect(drawX1 + FRAME_WIDTH,
                drawY1 + FRAME_WIDTH,
                drawX2 - FRAME_WIDTH,
                drawY2 - FRAME_WIDTH,
                paint)

        paint.color = oldColour
        canvas.drawText(text, start, end, x + FRAME_MARGIN, y.toFloat(), paint)
    }

    companion object {
        private const val FRAME_MARGIN = 3
        private const val FRAME_WIDTH = 1
    }
}

/**
 * Span used to render code blocks. These are blocks that are wrapped with triple-backquote.
 */
class CodeBlockTypefaceSpan : ReplacementSpan() {
    private val typeface = Typeface.MONOSPACE

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val bounds = Rect()
        paint.typeface = typeface
        paint.getTextBounds(text.toString(), start, end, bounds)
        return bounds.right
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        paint.typeface = typeface
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}

class CodeBlockBackgroundSpan(context: Context) : LineBackgroundSpan {
    private val colour = ContextCompat.getColor(context, R.color.code_block_background)

    override fun drawBackground(canvas: Canvas, paint: Paint,
                                left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
                                text: CharSequence?, start: Int, end: Int, lnum: Int) {
        val oldColour = paint.color
        paint.color = colour
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        paint.color = oldColour
    }
}
