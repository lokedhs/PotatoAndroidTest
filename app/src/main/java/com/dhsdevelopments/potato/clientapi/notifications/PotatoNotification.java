package com.dhsdevelopments.potato.clientapi.notifications;

import com.dhsdevelopments.potato.clientapi.message.Message;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PotatoNotification implements Serializable
{
    @SerializedName( "type" )
    public String type;


    @Override
    public String toString() {
        return "PotatoNotification[" +
                       "type='" + type + '\'' +
                       ']';
    }
}
