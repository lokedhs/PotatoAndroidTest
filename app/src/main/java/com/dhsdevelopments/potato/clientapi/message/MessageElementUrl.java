package com.dhsdevelopments.potato.clientapi.message;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;

public class MessageElementUrl extends MessageElement
{
    private String addr;
    private String description;

    public MessageElementUrl( String addr, String description ) {
        this.addr = addr;
        this.description = description;
    }


    @Override
    public CharSequence getSpannable() {
        Spannable s = new SpannableString( description );
        s.setSpan( new URLSpan( addr ), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return s;
    }

    @Override
    public String toString() {
        return "MessageElementUrl[" +
                       "addr='" + addr + '\'' +
                       ", description='" + description + '\'' +
                       "]";
    }
}
