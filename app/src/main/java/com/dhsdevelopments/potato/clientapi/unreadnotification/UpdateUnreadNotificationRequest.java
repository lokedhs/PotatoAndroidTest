package com.dhsdevelopments.potato.clientapi.unreadnotification;

import com.google.gson.annotations.SerializedName;

public class UpdateUnreadNotificationRequest
{
    @SerializedName( "token" )
    public String token;

    @SerializedName( "add" )
    public boolean add;

    public UpdateUnreadNotificationRequest( String token, boolean add ) {
        this.token = token;
        this.add = add;
    }
}
