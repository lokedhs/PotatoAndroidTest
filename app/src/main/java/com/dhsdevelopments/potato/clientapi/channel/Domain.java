package com.dhsdevelopments.potato.clientapi.channel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings( "unused" )
public class Domain
{
    @SerializedName( "id" )
    private String id;

    @SerializedName( "name" )
    private String name;

    @SerializedName( "groups" )
    private List<Group> groups;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return "Domain[" +
                       "id='" + id + '\'' +
                       ", name='" + name + '\'' +
                       ", groups=" + groups +
                       ']';
    }
}
