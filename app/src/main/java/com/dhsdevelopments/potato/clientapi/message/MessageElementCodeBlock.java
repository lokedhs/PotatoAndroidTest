package com.dhsdevelopments.potato.clientapi.message;

public class MessageElementCodeBlock extends MessageElement
{
    private final String language;
    private final String code;

    public MessageElementCodeBlock( String language, String code ) {
        this.language = language;
        this.code = code;
    }

    @Override
    public CharSequence getSpannable() {
        return code;
    }

    @Override
    public String toString() {
        return "MessageElementCodeBlock[" +
                       "language='" + language + '\'' +
                       ", code='" + code + '\'' +
                       "] " + super.toString();
    }
}
