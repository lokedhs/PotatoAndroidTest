package com.dhsdevelopments.potato.channellist;

import com.dhsdevelopments.potato.clientapi.Channel;
import com.dhsdevelopments.potato.clientapi.Domain;
import com.dhsdevelopments.potato.clientapi.Group;

import java.util.ArrayList;
import java.util.List;

class ChannelEntry
{
    private String id;
    private String name;

    ChannelEntry( String id, String name ) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    static List<ChannelEntry> makeFromChannelTree( List<Domain> domains ) {
        List<ChannelEntry> result = new ArrayList<>();
        for( Domain d : domains ) {
            for( Group g : d.getGroups() ) {
                for( Channel c : g.getChannels() ) {
                    result.add( new ChannelEntry( c.getId(), c.getName() ) );
                }
            }
        }
        return result;
    }
}
