package com.dhsdevelopments.potato.clientapi;

import com.google.gson.annotations.SerializedName;

public class ClearNotificationsResult
{
    @SerializedName( "result" )
    public String result;

    @Override
    public String toString() {
        return "ClearNotificationsResult[" +
                       "result='" + result + '\'' +
                       ']';
    }
}
