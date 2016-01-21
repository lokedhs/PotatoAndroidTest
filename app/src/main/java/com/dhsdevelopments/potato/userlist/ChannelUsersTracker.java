package com.dhsdevelopments.potato.userlist;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.channelmessages.HasUserTracker;
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult;
import com.dhsdevelopments.potato.clientapi.users.User;
import com.dhsdevelopments.potato.service.ChannelSubscriptionService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChannelUsersTracker
{
    private Context context;
    private String cid;
    private Map<String, UserDescriptor> users = new HashMap<>();
    private Set<UserActivityListener> listeners = new CopyOnWriteArraySet<>();

    private ChannelUsersTracker( Context context, String cid ) {
        this.context = context;
        this.cid = cid;
        loadUsers();
    }

    public Map<String, UserDescriptor> getUsers() {
        return users;
    }

    public static ChannelUsersTracker findForChannel( Context context, String cid ) {
        return new ChannelUsersTracker( context, cid );
    }

    public static ChannelUsersTracker findEnclosingUserTracker( Fragment fragment ) {
        FragmentActivity activity = fragment.getActivity();
        if( activity instanceof HasUserTracker ) {
            return ((HasUserTracker)activity).getUsersTracker();
        }
        else {
            return null;
        }
    }

    public void processIncoming( Intent intent ) {
        Log.i( "processing channel user intent: " + intent );
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
                processAddRemove( intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_USER_ID ), true, true );
                break;
            case ChannelSubscriptionService.USER_UPDATE_TYPE_REMOVE:
                processAddRemove( intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_USER_ID ), false, true );
                break;
        }
    }

    private void processAddRemove( String uid, boolean active, boolean fireEvent ) {
        UserDescriptor d = users.get( uid );
        if( d != null ) {
            d.active = active;
        }
        else {
            users.put( uid, new UserDescriptor( "noname", active ) );
        }

        if( fireEvent ) {
            for( UserActivityListener l : listeners ) {
                l.userUpdated( uid );
            }
        }
    }

    private void processSync( Intent intent ) {
        String[] uids = intent.getStringArrayExtra( ChannelSubscriptionService.EXTRA_CHANNEL_USERS_SYNC_USERS );
        Log.i( "Got sync message. userList = " + Arrays.toString( uids ) );
        // Clear the activate state of all current users
        for( UserDescriptor d : users.values() ) {
            d.active = false;
        }
        for( String uid : uids ) {
            processAddRemove( uid, true, false );
        }
        fireUserListSync();
    }

    private void updateUsers( List<User> members ) {
        for( User u : members ) {
            UserDescriptor d = users.get( u.id );
            if( d == null ) {
                users.put( u.id, new UserDescriptor( u.description, false ) );
            }
            else {
                d.name = u.description;
            }
        }
        fireUserListSync();
    }

    private void fireUserListSync() {
        for( UserActivityListener l : listeners ) {
            l.activeUserListSync();
        }
    }

    public void addUserActivityListener( UserActivityListener listener ) {
        listeners.add( listener );
    }

    public void removeUserActivityListener( UserActivityListener listener ) {
        listeners.remove( listener );
    }

    public void loadUsers() {
        PotatoApplication app = PotatoApplication.getInstance( context );
        Call<LoadUsersResult> call = app.getPotatoApi().loadUsers( app.getApiKey(), cid );
        call.enqueue( new Callback<LoadUsersResult>()
        {
            @Override
            public void onResponse( Response<LoadUsersResult> response, Retrofit retrofit ) {
                if( response.isSuccess() ) {
                    updateUsers( response.body().members );
                }
                else {
                    Log.wtf( "Error code from server" );
                }
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Error loading users", t );
            }
        } );
    }

    class UserDescriptor
    {
        private String name;
        private boolean active;

        public UserDescriptor( String name, boolean active ) {
            this.name = name;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }
    }

    public interface UserActivityListener
    {
        void activeUserListSync();

        void userUpdated( String uid );
    }
}
