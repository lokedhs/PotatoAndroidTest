package com.dhsdevelopments.potato.clientapi;

import java.util.List;

@SuppressWarnings( "unused" )
public class Group
{
    private String id;
    private String name;
    private List<Channel> channels;

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
                       ']';
    }
}
