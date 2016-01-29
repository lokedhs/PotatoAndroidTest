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

    ChannelEntry( String id, String domainName, String groupName, String channelName ) {
        this.id = id;
        this.domainName = domainName;
        this.groupName = groupName;
        this.name = channelName;
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

    static List<ChannelEntry> makeFromChannelTree( Domain d ) {
        List<ChannelEntry> result = new ArrayList<>();
        String domainName = d.getName();
        for( Group g : d.getGroups() ) {
            String groupName = g.getName();
            for( Channel c : g.getChannels() ) {
                result.add( new ChannelEntry( c.getId(), domainName, groupName, c.getName() ) );
            }
        }
        return result;
    }
}
