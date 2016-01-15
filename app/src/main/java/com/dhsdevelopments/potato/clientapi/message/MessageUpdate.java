package com.dhsdevelopments.potato.clientapi.message;

import com.google.gson.annotations.SerializedName;

public class MessageUpdate
{
    @SerializedName( "message" )
    public String message;

    @SerializedName( "date" )
    public String date;

    @SerializedName( "deleted" )
    public boolean deleted;

    @Override
    public String toString() {
        return "MessageUpdate[" +
                       "message='" + message + '\'' +
                       ", date='" + date + '\'' +
                       ", deleted=" + deleted +
                       ']';
    }
}
