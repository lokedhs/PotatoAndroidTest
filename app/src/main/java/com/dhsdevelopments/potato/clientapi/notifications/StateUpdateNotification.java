package com.dhsdevelopments.potato.clientapi.notifications;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StateUpdateNotification extends PotatoNotification
{
    @SerializedName( "add-type" )
    public String addType;

    @SerializedName( "user" )
    public String userStateUser;

    @SerializedName( "users" )
    public List<UserStateUpdateUser> userStateSyncMembers;

    @SerializedName( "channel" )
    public String channel;

    @Override
    public String toString() {
        return "StateUpdateNotification[" +
                       "addType='" + addType + '\'' +
                       ", userStateUser='" + userStateUser + '\'' +
                       ", userStateSyncMembers=" + userStateSyncMembers +
                       ", channel='" + channel + '\'' +
                       "] " + super.toString();
    }
}
