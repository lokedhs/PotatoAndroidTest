package com.dhsdevelopments.potato.clientapi.users;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoadUsersResult
{
    @SerializedName( "members" )
    public List<User> members;

    @Override
    public String toString() {
        return "LoadUsersResult[" +
                       "members=" + members +
                       ']';
    }
}
