package com.dhsdevelopments.potato.clientapi.message;

import java.io.Serializable;

public abstract class MessageElement implements Serializable
{
    public CharSequence getSpannable() {
        return "[NOT-IMPLEMENTED type=" + getClass().getName() + "]";
    }
}
