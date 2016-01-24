package com.dhsdevelopments.potato.clientapi.message;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;

public class MessageElementUser extends MessageElement
{
    private String userId;
    private String userDescription;

    public MessageElementUser( String userId, String userDescription ) {
        this.userId = userId;
        this.userDescription = userDescription;
    }

    @Override
    public CharSequence getSpannable() {
        Spannable s = new SpannableString( userDescription );
        s.setSpan( new BackgroundColorSpan( Color.rgb( 0xe3, 0xe3, 0xe3 ) ), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return s;
    }
}
