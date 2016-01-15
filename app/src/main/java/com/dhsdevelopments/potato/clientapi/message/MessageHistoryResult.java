package com.dhsdevelopments.potato.clientapi.message;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageHistoryResult
{
    @SerializedName( "messages" )
    private List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "MessageHistoryResult[" +
                       "messages=" + messages +
                       ']';
    }
}
