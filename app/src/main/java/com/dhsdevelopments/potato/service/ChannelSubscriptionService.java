package com.dhsdevelopments.potato.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult;
import retrofit.Call;
import retrofit.Response;

import java.io.IOException;
import java.util.List;

public class ChannelSubscriptionService extends Service
{
    public static final String ACTION_BIND_TO_CHANNEL = "bindToChannel";
    public static final String EXTRA_CHANNEL_ID = "channelId";

    public static final String ACTION_MESSAGE_RECEIVED = ChannelSubscriptionService.class.getName() + ".MESSAGE_RECEIVED";
    public static final String EXTRA_MESSAGE = "message";

    private Thread receiverThread = null;

    public ChannelSubscriptionService() {
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        String action = intent.getAction();
        if( action.equals( ACTION_BIND_TO_CHANNEL ) ) {
            bindToChannel( intent.getStringExtra( EXTRA_CHANNEL_ID ) );
        }
        else {
            throw new UnsupportedOperationException( "Illegal subscription command: " + action );
        }
        return START_NOT_STICKY;
    }

    private void bindToChannel( String cid ) {
        Log.i( "Binding to channel: " + cid );
        if( receiverThread == null ) {
            receiverThread = new Receiver( cid );
            receiverThread.start();
        }
        else {
            throw new UnsupportedOperationException( "Channel binding not supported yet" );
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

        public Receiver( String cid ) {
            super( "NotificationReceiver" );
            this.cid = cid;
            PotatoApplication app = PotatoApplication.getInstance( ChannelSubscriptionService.this );
            api = app.getPotatoApi();
            apiKey = app.getApiKey();
        }

        @Override
        public void run() {
            Handler handler = new Handler( ChannelSubscriptionService.this.getMainLooper() );

            String eventId = null;
            try {
                while( !interrupted() ) {
                    Call<PotatoNotificationResult> call = api.channelUpdates( apiKey, cid, "content", eventId );
                    try {
                        Response<PotatoNotificationResult> response = call.execute();
                        if( response.isSuccess() ) {
                            PotatoNotificationResult body = response.body();
                            eventId = body.eventId;

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
                    catch( IOException e ) {
                        // If an error occurs, wait for a while before trying again
                        Thread.sleep( 10000 );
                    }
                }
            }
            catch( InterruptedException e ) {
                Log.e( "Should exit normally here, but this hasn't been tested yet", e );
            }
        }


    }
}
