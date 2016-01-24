package com.dhsdevelopments.potato.clientapi.notifications;

import com.google.gson.annotations.SerializedName;

public class TypingNotification extends PotatoNotification
{
    @SerializedName( "user" )
    public String userId;

    @SerializedName( "channel" )
    public String channelId;

    @SerializedName( "add-type" )
    public String addType;
}
