package com.dhsdevelopments.potato.clientapi.message;

public abstract class MessageElement
{
    public CharSequence getSpannable() {
        return "[NOT-IMPLEMENTED type=" + getClass().getName() + "]";
    }
}
