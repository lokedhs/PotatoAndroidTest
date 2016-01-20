package com.dhsdevelopments.potato.clientapi.users;

import com.google.gson.annotations.SerializedName;

public class User
{
    @SerializedName( "id" )
    public String id;

    @SerializedName( "description" )
    public String description;

    @SerializedName( "nickname" )
    public String nickname;

    @SerializedName( "image_name" )
    public String imageName;

    @Override
    public String toString() {
        return "User[" +
                       "id='" + id + '\'' +
                       ", description='" + description + '\'' +
                       ", nickname='" + nickname + '\'' +
                       ", imageName='" + imageName + '\'' +
                       ']';
    }
}
