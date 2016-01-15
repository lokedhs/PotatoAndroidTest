package com.dhsdevelopments.potato.clientapi.message;

import android.text.SpannableStringBuilder;

import java.util.List;

public class MessageElementList extends MessageElement
{
    private List<MessageElement> list;

    public MessageElementList( List<MessageElement> list ) {
        this.list = list;
    }

    @Override
    public CharSequence getSpannable() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for( MessageElement element : list ) {
            builder.append( element.getSpannable() );
        }
        return builder;
    }

    @Override
    public String toString() {
        return "MessageElementList[" +
                       "list=" + list +
                       "] " + super.toString();
    }
}
