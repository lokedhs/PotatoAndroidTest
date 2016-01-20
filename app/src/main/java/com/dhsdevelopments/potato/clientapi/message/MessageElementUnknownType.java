package com.dhsdevelopments.potato.clientapi.message;

public class MessageElementUnknownType extends MessageElement
{
    private String type;

    public MessageElementUnknownType( String type ) {
        this.type = type;
    }

    public CharSequence getSpannable() {
        return "[TYPE=" + type + "]";
    }

    @Override
    public String toString() {
        return "MessageElementUnknownType[" +
                       "type='" + type + '\'' +
                       ']';
    }
}
