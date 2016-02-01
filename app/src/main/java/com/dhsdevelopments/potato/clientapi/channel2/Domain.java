package com.dhsdevelopments.potato.clientapi.channel2;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Domain
{
    @SerializedName( "id" )
    public String id;

    @SerializedName( "name" )
    public String name;

    @SerializedName( "domain-type" )
    public String type;

    @SerializedName( "channels" )
    public List<Channel> channels;

    @Override
    public String toString() {
        return "Domain[" +
                       "id='" + id + '\'' +
                       ", name='" + name + '\'' +
                       ", type='" + type + '\'' +
                       ", channels=" + channels +
                       ']';
    }
}
