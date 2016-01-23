package com.dhsdevelopments.potato.editor;

class UserSuggestion
{
    private String id;
    private String name;

    public UserSuggestion( String id, String name ) {
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
