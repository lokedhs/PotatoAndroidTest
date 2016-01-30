package com.dhsdevelopments.potato.clientapi.channel2;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChannelsResult
{
    @SerializedName( "domains" )
    public List<Domain> domains;

    @Override
    public String toString() {
        return "ChannelsResult[" +
                       "domains=" + domains +
                       ']';
    }
}
