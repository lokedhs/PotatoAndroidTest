package com.dhsdevelopments.potato.clientapi.deletemessage;

import com.google.gson.annotations.SerializedName;

public class DeleteMessageResult
{
    @SerializedName( "result" )
    public String result;

    @SerializedName( "id" )
    public String messageId;
}
