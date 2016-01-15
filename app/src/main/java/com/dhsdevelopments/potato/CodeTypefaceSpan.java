package com.dhsdevelopments.potato;

import android.graphics.*;
import android.text.style.ReplacementSpan;

public class CodeTypefaceSpan extends ReplacementSpan
{
    private Typeface typeface = Typeface.MONOSPACE;
    private static final int FRAME_MARGIN = 3;
    private static final int FRAME_WIDTH = 1;

    @Override
    public int getSize( Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm ) {
        Rect bounds = new Rect();
        paint.setTypeface( typeface );
        paint.getTextBounds( text.toString(), start, end, bounds );
        return bounds.right + FRAME_MARGIN * 2;
    }

    @SuppressWarnings( "UnnecessaryLocalVariable" )
    @Override
    public void draw( Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint ) {
        paint.setTypeface( typeface );

        Rect bounds = new Rect();
        paint.getTextBounds( text.toString(), start, end, bounds );

        Paint.FontMetrics metrics = new Paint.FontMetrics();
        paint.getFontMetrics( metrics );

        int oldColour = paint.getColor();

        paint.setColor( Color.rgb( 120, 120, 120 ) );
        float drawX1 = x;
        float drawY1 = y + metrics.top;
        float drawX2 = x + bounds.right + FRAME_MARGIN * 2;
        float drawY2 = y + metrics.bottom;
        canvas.drawRect( drawX1, drawY1, drawX2, drawY2, paint );

        paint.setColor( Color.rgb( 210, 210, 210 ) );
        canvas.drawRect( drawX1 + FRAME_WIDTH,
                         drawY1 + FRAME_WIDTH,
                         drawX2 - FRAME_WIDTH,
                         drawY2 - FRAME_WIDTH,
                         paint );

        paint.setColor( oldColour );
        canvas.drawText( text, start, end, x + FRAME_MARGIN, y, paint );
    }
}
