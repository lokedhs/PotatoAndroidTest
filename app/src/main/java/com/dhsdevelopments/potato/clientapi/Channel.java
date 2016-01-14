package com.dhsdevelopments.potato.clientapi;

@SuppressWarnings( "unused" )
public class Channel
{
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Channel[" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ']';
    }
}
