package com.dhsdevelopments.potato.userlist;

public class UserWrapper
{
    private String id;
    private String name;

    public UserWrapper( String id, String name ) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
