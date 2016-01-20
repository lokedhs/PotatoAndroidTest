package com.dhsdevelopments.potato.clientapi.message;

public class MessageElementNewline extends MessageElement
{
    @Override
    public CharSequence getSpannable() {
        return "\n";
    }
}
