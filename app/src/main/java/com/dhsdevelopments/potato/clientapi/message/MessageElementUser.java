package com.dhsdevelopments.potato.clientapi.message;

public class MessageElementUser extends MessageElement
{
    private String userId;
    private String userDescription;

    public MessageElementUser( String userId, String userDescription ) {
        this.userId = userId;
        this.userDescription = userDescription;
    }

    @Override
    public CharSequence getSpannable() {
        return userDescription;
    }
}
