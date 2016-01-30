package com.dhsdevelopments.potato.clientapi.channel2;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Domain
{
    @SerializedName( "id" )
    public String id;

    @SerializedName( "name" )
    public String name;

    @SerializedName( "channels" )
    public List<Channel> channels;

    @Override
    public String toString() {
        return "Domain[" +
                       "id='" + id + '\'' +
                       ", name='" + name + '\'' +
                       ", channels=" + channels +
                       ']';
    }
}
