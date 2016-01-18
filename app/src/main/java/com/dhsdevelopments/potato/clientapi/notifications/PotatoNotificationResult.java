package com.dhsdevelopments.potato.clientapi.notifications;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PotatoNotificationResult
{
    @SerializedName( "event" )
    public String eventId;

    @SerializedName( "data" )
    public List<PotatoNotification> notifications;
}
