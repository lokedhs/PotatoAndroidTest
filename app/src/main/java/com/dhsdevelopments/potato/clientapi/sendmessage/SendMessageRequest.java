package com.dhsdevelopments.potato.clientapi.sendmessage;

import com.google.gson.annotations.SerializedName;

public class SendMessageRequest
{
    @SerializedName( "text" )
    public String text;

    public SendMessageRequest() {
    }

    public SendMessageRequest( String text ) {
        this.text = text;
    }
}
