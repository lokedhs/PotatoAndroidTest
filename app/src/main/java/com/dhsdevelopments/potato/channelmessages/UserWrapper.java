package com.dhsdevelopments.potato.channelmessages;

import com.dhsdevelopments.potato.clientapi.users.User;

public class UserWrapper
{
    private String id;
    private String name;

    public UserWrapper( User user ) {
        id = user.id;
        fillInFromUser( user );
    }

    public void fillInFromUser( User user ) {
        name = user.description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
