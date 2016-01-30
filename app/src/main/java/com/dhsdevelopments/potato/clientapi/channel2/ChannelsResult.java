package com.dhsdevelopments.potato.clientapi.channel2;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChannelsResult
{
    @SerializedName( "channels" )
    public List<Channel> channels;

    @Override
    public String toString() {
        return "ChannelsResult[" +
                       "channels=" + channels +
                       ']';
    }
}
