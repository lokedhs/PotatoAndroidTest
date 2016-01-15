package com.dhsdevelopments.potato.clientapi.message;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

public class MessageElementBold extends TypedMessageElement
{
    public MessageElementBold( MessageElement content ) {
        super( content );
    }

    @Override
    public CharSequence getSpannable() {
        Spannable s = new SpannableString( content.getSpannable() );
        s.setSpan( new StyleSpan( Typeface.BOLD ), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return s;
    }
}
