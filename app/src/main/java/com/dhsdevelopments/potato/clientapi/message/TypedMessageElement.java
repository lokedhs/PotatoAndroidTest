package com.dhsdevelopments.potato.clientapi.message;

public class TypedMessageElement extends MessageElement
{
    protected MessageElement content;

    public TypedMessageElement( MessageElement content ) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TypedMessageElement[type=" + getClass().getName() +
                       ", content=" + content +
                       "] " + super.toString();
    }
}
