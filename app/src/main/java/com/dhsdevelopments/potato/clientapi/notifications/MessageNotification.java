package com.dhsdevelopments.potato.clientapi.notifications;

import com.dhsdevelopments.potato.clientapi.message.Message;
import com.google.gson.annotations.SerializedName;

public class MessageNotification extends PotatoNotification
{
    @SerializedName( "c" )
    public Message message;
}
