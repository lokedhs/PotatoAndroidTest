package com.dhsdevelopments.potato.clientapi.message;

import android.text.Spannable;
import android.text.SpannableString;
import com.dhsdevelopments.potato.CodeTypefaceSpan;

public class MessageElementCode extends TypedMessageElement
{
    public MessageElementCode( MessageElement content ) {
        super( content );
    }

    @Override
    public CharSequence getSpannable() {
        Spannable s = new SpannableString( content.getSpannable() );
        s.setSpan( new CodeTypefaceSpan(), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return s;
    }
}
