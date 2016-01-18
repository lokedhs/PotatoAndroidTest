package com.dhsdevelopments.potato.clientapi.notifications;

import com.dhsdevelopments.potato.clientapi.message.Message;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PotatoNotification implements Serializable
{
    @SerializedName( "type" )
    public String type;

    @SerializedName( "c" )
    public Message message;

    public boolean isMessage() {
        return type.equals( "m" );
    }

    @Override
    public String toString() {
        return "PotatoNotification[" +
                       "type='" + type + '\'' +
                       ", message=" + message +
                       ']';
    }
}
