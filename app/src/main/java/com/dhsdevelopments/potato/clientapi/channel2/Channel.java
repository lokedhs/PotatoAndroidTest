package com.dhsdevelopments.potato.clientapi.channel2;

import com.google.gson.annotations.SerializedName;

public class Channel
{
    @SerializedName( "id" )
    public String id;

    @SerializedName( "name" )
    public String name;

    @SerializedName( "hide" )
    public boolean hide;

    @SerializedName( "group" )
    public String groupId;

    @SerializedName( "group_type" )
    public String groupType;

    @SerializedName( "unread_count" )
    public int unreadCount;

    @Override
    public String toString() {
        return "Channel[" +
                       "id='" + id + '\'' +
                       ", name='" + name + '\'' +
                       ", hide=" + hide +
                       ", groupId='" + groupId + '\'' +
                       ", groupType='" + groupType + '\'' +
                       ", unreadCount=" + unreadCount +
                       ']';
    }
}
