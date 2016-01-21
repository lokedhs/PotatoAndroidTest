package com.dhsdevelopments.potato.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.clientapi.ChannelUpdatesUpdateResult;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelSubscriptionService extends Service
{
    public static final String ACTION_BIND_TO_CHANNEL = "com.dhsdevelopments.potato.BIND_CHANNEL";
    public static final String ACTION_UNBIND_FROM_CHANNEL = "com.dhsdevelopments.potato.UNBIND_CHANNEL";
    public static final String EXTRA_CHANNEL_ID = "channelId";

    public static final String ACTION_MESSAGE_RECEIVED = "com.dhsdevelopments.potato.MESSAGE_RECEIVED";
    public static final String EXTRA_MESSAGE = "com.dhsdevelopments.potato.message";

    public static final String ACTION_CHANNEL_USERS_UPDATE = "com.dhsdevelopments.potato.CHANNEL_USERS_UPDATED";
    public static final String EXTRA_CHANNEL_USERS_SYNC_USERS = "com.dhsdevelopments.potato.sync_users";
    public static final String EXTRA_CHANNEL_USERS_USER_ID = "com.dhsdevelopments.potato.update_user";
    public static final String EXTRA_CHANNEL_USERS_TYPE = "com.dhsdevelopments.potato.update_user_add_type";
    public static final String USER_UPDATE_TYPE_SYNC = "sync";
    public static final String USER_UPDATE_TYPE_ADD = "add";
    public static final String USER_UPDATE_TYPE_REMOVE = "remove";

    private Receiver receiverThread = null;

    public ChannelSubscriptionService() {
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        String action = intent.getAction();
        switch( action ) {
            case ACTION_BIND_TO_CHANNEL:
                bindToChannel( intent.getStringExtra( EXTRA_CHANNEL_ID ) );
                break;
            case ACTION_UNBIND_FROM_CHANNEL:
                unbindFromChannel( intent.getStringExtra( EXTRA_CHANNEL_ID ) );
                break;
            default:
                throw new UnsupportedOperationException( "Illegal subscription command: " + action );
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i( "Destroying subscription service" );
        if( receiverThread != null ) {
            receiverThread.requestShutdown();
        }
        super.onDestroy();
    }

    private void unbindFromChannel( String cid ) {
        if( receiverThread == null ) {
            Log.w( "Attempt to unbind with no thread running" );
        }
        else {
            boolean wasShutdown = receiverThread.unbindFromChannel( cid );
            if( wasShutdown ) {
                receiverThread = null;
            }
        }
    }

    private void bindToChannel( String cid ) {
        Log.i( "Binding to channel: " + cid );
        if( receiverThread == null ) {
            receiverThread = new Receiver( cid );
            receiverThread.start();
        }
        else {
            receiverThread.bindToChannel( cid );
        }
    }

    private void processNewNotifications( List<PotatoNotification> notifications ) {
        for( PotatoNotification n : notifications ) {
            Log.i( "Processing notification: " + n );
            if( n.isMessage() ) {
                Message msg = n.message;
                Intent intent = new Intent( ACTION_MESSAGE_RECEIVED );
                intent.putExtra( EXTRA_MESSAGE, msg );
                sendBroadcast( intent );
            }
            else if( n.isStateUpdate() ) {
                Intent intent = new Intent( ACTION_CHANNEL_USERS_UPDATE );
                intent.putExtra( EXTRA_CHANNEL_ID, n.channel );
                switch( n.addType ) {
                    case "sync":
                        intent.putExtra( EXTRA_CHANNEL_USERS_TYPE, USER_UPDATE_TYPE_SYNC );
                        intent.putExtra( EXTRA_CHANNEL_USERS_SYNC_USERS, n.userStateSyncMembers.toArray( new String[n.userStateSyncMembers.size()] ) );
                        sendBroadcast( intent );
                        break;
                    case "add":
                        intent.putExtra( EXTRA_CHANNEL_USERS_TYPE, USER_UPDATE_TYPE_ADD );
                        intent.putExtra( EXTRA_CHANNEL_USERS_USER_ID, n.userStateUser );
                        sendBroadcast( intent );
                        break;
                    case "remove":
                        intent.putExtra( EXTRA_CHANNEL_USERS_TYPE, USER_UPDATE_TYPE_REMOVE );
                        intent.putExtra( EXTRA_CHANNEL_USERS_USER_ID, n.userStateUser );
                        sendBroadcast( intent );
                        break;
                    default:
                        Log.w( "Unexpected addType: " + n.addType );
                }
            }
        }
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    private class Receiver extends Thread
    {
        private PotatoApi api;
        private String apiKey;
        private String cid;

        private boolean shutdown = false;
        private Set<String> subscribedChannels = new HashSet<>();
        private Set<String> pendingBinds = new HashSet<>();
        private String eventId = null;
        private Call<PotatoNotificationResult> outstandingCall = null;

        public Receiver( String cid ) {
            super( "NotificationReceiver" );
            this.cid = cid;
            PotatoApplication app = PotatoApplication.getInstance( ChannelSubscriptionService.this );

            api = app.getPotatoApiLongTimeout();
            apiKey = app.getApiKey();

            subscribedChannels.add( cid );
        }

        public synchronized String getEventId() {
            return eventId;
        }

        @Override
        public void run() {
            Handler handler = new Handler( ChannelSubscriptionService.this.getMainLooper() );

            try {
                while( !isShutdown() && !interrupted() ) {
                    Call<PotatoNotificationResult> call = api.channelUpdates( apiKey, cid, "content,state", getEventId() );
                    synchronized( this ) {
                        outstandingCall = call;
                    }
                    try {
                        Response<PotatoNotificationResult> response = call.execute();

                        synchronized( this ) {
                            outstandingCall = null;
                        }

                        if( response.isSuccess() ) {
                            PotatoNotificationResult body = response.body();

                            updateEventIdAndCheckPendingBindRequests( body.eventId );

                            final List<PotatoNotification> notifications = body.notifications;
                            if( notifications != null && !notifications.isEmpty() ) {
                                handler.post( new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        processNewNotifications( notifications );
                                    }
                                } );
                            }
                        }
                        else {
                            Log.e( "Error reading notifications: " + response.message() );
                            Thread.sleep( 10000 );
                        }
                    }
                    catch( InterruptedIOException e ) {
                        throw new ReceiverStoppedException( e );
                    }
                    catch( IOException e ) {
                        // If an error occurs, wait for a while before trying again
                        if( !isShutdown() ) {
                            Log.e( "Got exception when waiting for updates", e );
                            Thread.sleep( 10000 );
                        }
                    }
                }
            }
            catch( InterruptedException e ) {
                if( !isShutdown() ) {
                    Log.wtf( "Got interruption while not being shutdown", e );
                }
            }
            catch( ReceiverStoppedException e ) {
                if( !isShutdown() ) {
                    Log.wtf( "Receiver stop requested while not in shutdown state", e );
                }
            }
            Log.i( "Updates thread shut down" );
        }

        private void updateEventIdAndCheckPendingBindRequests( String eventId ) {
            if( eventId == null ) {
                throw new IllegalStateException( "Received eventId was null when updating" );
            }

            Set<String> pendingBindsCopy = null;
            synchronized( this ) {
                this.eventId = eventId;
                if( shutdown ) {
                    return;
                }
                if( !pendingBinds.isEmpty() ) {
                    pendingBindsCopy = pendingBinds;
                    pendingBinds = null;
                }
            }
            if( pendingBindsCopy != null ) {
                for( String s : pendingBindsCopy ) {
                    submitBindRequest( s );
                }
            }
        }


        public void bindToChannel( String cid ) {
            boolean willAdd = false;
            synchronized( this ) {
                if( shutdown ) {
                    Log.w( "Not binding since the connection is being shut down" );
                    return;
                }
                if( !subscribedChannels.contains( cid ) ) {
                    subscribedChannels.add( cid );
                    if( eventId == null ) {
                        pendingBinds.add( cid );
                    }
                    else {
                        willAdd = true;
                    }
                }
            }
            if( willAdd ) {
                submitBindRequest( cid );
            }
        }

        private void submitBindRequest( String cid ) {
            Log.i( "Submit bind request: " + cid );
            if( getEventId() == null ) {
                throw new IllegalStateException( "eventId is null" );
            }
            Call<ChannelUpdatesUpdateResult> call = api.channelUpdatesUpdate( apiKey, getEventId(), "add", cid, "content,state" );
            call.enqueue( new Callback<ChannelUpdatesUpdateResult>()
            {
                @Override
                public void onResponse( Response<ChannelUpdatesUpdateResult> response, Retrofit retrofit ) {
                    if( response.isSuccess() ) {
                        if( !"ok".equals( response.body().result ) ) {
                            Log.wtf( "Unexpected result form bind call" );
                        }
                    }
                    else {
                        Log.wtf( "Got failure from server: " + response.message() );
                    }
                }

                @Override
                public void onFailure( Throwable t ) {
                    Log.wtf( "Failed to bind", t );
                }
            } );
        }

        public boolean unbindFromChannel( String cid ) {
            boolean wasShutdown = false;
            synchronized( this ) {
                subscribedChannels.remove( cid );
                pendingBinds.remove( cid );
                if( subscribedChannels.isEmpty() ) {
                    requestShutdown();
                    wasShutdown = true;
                }
            }
            // TODO: Send unbind request to server here
            return wasShutdown;
        }

        private void requestShutdown() {
            Call<PotatoNotificationResult> outstandingCallCopy;
            synchronized( this ) {
                shutdown = true;
                outstandingCallCopy = outstandingCall;
            }
            if( outstandingCallCopy != null ) {
                outstandingCallCopy.cancel();
            }

            interrupt();
        }

        public synchronized boolean isShutdown() {
            return shutdown;
        }
    }
}
