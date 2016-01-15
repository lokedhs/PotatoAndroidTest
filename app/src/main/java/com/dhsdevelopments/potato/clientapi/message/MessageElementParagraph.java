package com.dhsdevelopments.potato.clientapi.message;

public class MessageElementParagraph extends TypedMessageElement
{
    public MessageElementParagraph( MessageElement content ) {
        super( content );
    }

    @Override
    public CharSequence getSpannable() {
        return content.getSpannable();
    }
}
