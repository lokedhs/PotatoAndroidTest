package com.dhsdevelopments.potato.clientapi.notifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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
