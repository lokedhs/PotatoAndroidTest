package com.dhsdevelopments.potato.channellist;

public class ChannelEntry
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
