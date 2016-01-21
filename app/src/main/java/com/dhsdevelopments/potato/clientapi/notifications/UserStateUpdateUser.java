package com.dhsdevelopments.potato.clientapi.notifications;

import com.google.gson.annotations.SerializedName;

public class UserStateUpdateUser
{
    @SerializedName( "id" )
    public String id;

    @Override
    public String toString() {
        return "UserStateUpdateUser[" +
                       "id='" + id + '\'' +
                       ']';
    }
}
