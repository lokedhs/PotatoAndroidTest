package com.dhsdevelopments.potato.clientapi.channel;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings( "unused" )
public class Channel
{
    @SerializedName( "id" )
    private String id;

    @SerializedName( "name" )
    private String name;

    @SerializedName( "private" )
    private boolean privateChannel;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivateChannel() {
        return privateChannel;
    }

    @Override
    public String toString() {
        return "Channel[" +
                       "id='" + id + '\'' +
                       ", name='" + name + '\'' +
                       ", privateChannel=" + privateChannel +
                       ']';
    }
}
