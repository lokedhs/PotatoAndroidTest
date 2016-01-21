package com.dhsdevelopments.potato.clientapi.notifications;

import com.dhsdevelopments.potato.clientapi.message.Message;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PotatoNotification implements Serializable
{
    @SerializedName( "type" )
    public String type;

    @SerializedName( "c" )
    public Message message;

    @SerializedName( "add-type" )
    public String addType;

    @SerializedName( "user" )
    public String userStateUser;

    @SerializedName( "users" )
    public List<UserStateUpdateUser> userStateSyncMembers;

    @SerializedName( "channel" )
    public String channel;

    public boolean isMessage() {
        return type.equals( "m" );
    }

    public boolean isStateUpdate() {
        return type.equals( "cu" );
    }

    @Override
    public String toString() {
        return "PotatoNotification[" +
                       "type='" + type + '\'' +
                       ']';
    }
}
