package com.dhsdevelopments.potato.clientapi;

import java.util.List;

@SuppressWarnings( "unused" )
public class Domain
{
    private String id;
    private String name;
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
