package com.dhsdevelopments.potato.db;

public class DomainDescriptor
{
    private String id;
    private String name;

    public DomainDescriptor( String id, String name ) {
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
