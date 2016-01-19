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
    public static final String ACTION_BIND_TO_CHANNEL = "bindChannel";
    public static final String ACTION_UNBIND_FROM_CHANNEL = "unbindChannel";
    public static final String EXTRA_CHANNEL_ID = "channelId";

    public static final String ACTION_MESSAGE_RECEIVED = ChannelSubscriptionService.class.getName() + ".MESSAGE_RECEIVED";
    public static final String EXTRA_MESSAGE = "message";

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
                while( !interrupted() ) {
                    Call<PotatoNotificationResult> call = api.channelUpdates( apiKey, cid, "content", getEventId() );
                    try {
                        Response<PotatoNotificationResult> response = call.execute();
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
                        Log.e( "Got exception when waiting for updates", e );
                        Thread.sleep( 10000 );
                    }
                }
            }
            catch( InterruptedException e ) {
                if( !shutdown ) {
                    Log.wtf( "Got interruption while not being shutdown", e );
                }
            }
            catch( ReceiverStoppedException e ) {
                if( !shutdown ) {
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
            Call<ChannelUpdatesUpdateResult> call = api.channelUpdatesUpdate( apiKey, getEventId(), "add", cid, "content" );
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
            shutdown = true;
            interrupt();
        }
    }
}
