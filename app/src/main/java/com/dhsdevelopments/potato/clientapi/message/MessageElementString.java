package com.dhsdevelopments.potato.clientapi.message;

public class MessageElementString extends MessageElement
{
    private String value;

    public MessageElementString( String value ) {
        this.value = value;
    }

    @Override
    public CharSequence getSpannable() {
        return value;
    }

    @Override
    public String toString() {
        return "MessageElementString[" +
                       "value='" + value + '\'' +
                       "] " + super.toString();
    }
}
