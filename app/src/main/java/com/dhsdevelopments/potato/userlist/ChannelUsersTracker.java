package com.dhsdevelopments.potato.userlist;

import android.content.Intent;
import com.dhsdevelopments.potato.service.ChannelSubscriptionService;

import java.util.HashMap;
import java.util.Map;

public class ChannelUsersTracker
{
    private String cid;
    private Map<String, UserDescriptor> users = new HashMap<>();

    private ChannelUsersTracker( String cid ) {
        this.cid = cid;
    }

    public static ChannelUsersTracker findForChannel( String cid ) {
        return new ChannelUsersTracker( cid );
    }

    public void processIncoming( Intent intent ) {
        if( !intent.getAction().equals( ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE ) ) {
            // We only want to process channel users notifications
            return;
        }

        if( !intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_ID ).equals( cid ) ) {
            // Only accept notifications for the given channel
            return;
        }

        switch( intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_TYPE ) ) {
            case ChannelSubscriptionService.USER_UPDATE_TYPE_SYNC:
                processSync( intent );
                break;
            case ChannelSubscriptionService.USER_UPDATE_TYPE_ADD:
                processAddRemove( intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_USER_ID ), true );
                break;
            case ChannelSubscriptionService.USER_UPDATE_TYPE_REMOVE:
                processAddRemove( intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_USER_ID ), false );
                break;
        }
    }

    private void processAddRemove( String uid, boolean active ) {
        UserDescriptor d = users.get( uid );
        if( d != null ) {
            d.active = active;
        }
        else {
            users.put( uid, new UserDescriptor( "noname", active ) );
        }
    }

    private void processSync( Intent intent ) {
        // Clear the activate state of all current users
        for( UserDescriptor d : users.values() ) {
            d.active = false;
        }
        for( String uid : intent.getStringArrayExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_SYNC_USERS ) ) {
            processAddRemove( uid, true );
        }
    }

    private class UserDescriptor
    {
        private String name;
        private boolean active;

        public UserDescriptor( String name, boolean active ) {
            this.name = name;
            this.active = active;
        }
    }
}
