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
    private String domainName;
    private String groupName;
    private String name;
    private boolean privateChannel;

    ChannelEntry( String id, String domainName, String groupName, String channelName, boolean privateChannel ) {
        this.id = id;
        this.domainName = domainName;
        this.groupName = groupName;
        this.name = channelName;
        this.privateChannel = privateChannel;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isPrivateChannel() {
        return privateChannel;
    }
}
