package com.dhsdevelopments.potato.clientapi.channel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings( "unused" )
public class Group
{
    @SerializedName( "id" )
    private String id;

    @SerializedName( "name" )
    private String name;

    @SerializedName( "channels" )
    private List<Channel> channels;

    @SerializedName( "type" )
    private String type;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public String toString() {
        return "Group[" +
                       "id='" + id + '\'' +
                       ", name='" + name + '\'' +
                       ", channels=" + channels +
                       ", type='" + type + '\'' +
                       ']';
    }
}
