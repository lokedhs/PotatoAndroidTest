package com.dhsdevelopments.potato.channellist;

import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.clientapi.channel.Channel;
import com.dhsdevelopments.potato.clientapi.channel.Domain;
import com.dhsdevelopments.potato.clientapi.channel.Group;

import java.util.ArrayList;
import java.util.List;

class ChannelEntry
{
    private String id;
    private String name;
    private boolean privateChannel;
    private int unread;

    ChannelEntry( String id, String channelName, boolean privateChannel, int unread ) {
        this.id = id;
        this.name = channelName;
        this.privateChannel = privateChannel;
        this.unread = unread;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivateChannel() {
        return privateChannel;
    }

    public int getUnread() {
        return unread;
    }
}
