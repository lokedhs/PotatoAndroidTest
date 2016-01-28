package com.dhsdevelopments.potato.messages;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity;
import com.dhsdevelopments.potato.channelmessages.ChannelContentFragment;
import com.google.android.gms.gcm.GcmListenerService;

public class PotatoGcmListenerService extends GcmListenerService
{
    public PotatoGcmListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d( "PotatoGcmListenerService created" );
    }

    @Override
    public void onDestroy() {
        Log.d( "PotatoGcmListenerService destroyed" );
        super.onDestroy();
    }

    @Override
    public void onMessageReceived( String from, Bundle data ) {
        Log.d( "GCM message. from=" + from + ", data=" + data );

        String messageId = data.getString( "message_id" );
        String notificationType = data.getString( "notification_type" );
        String text = data.getString( "text" );
        String senderId = data.getString( "sender_id" );
        String senderName = data.getString( "sender_name" );
        String channelId = data.getString( "channel" );

        Intent intent = new Intent( this, ChannelContentActivity.class );
        intent.putExtra( ChannelContentFragment.ARG_CHANNEL_ID, channelId );
        intent.putExtra( ChannelContentFragment.ARG_CHANNEL_NAME, "Unknown" );
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_ONE_SHOT );

        NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder( this )
                .setSmallIcon( android.R.drawable.ic_dialog_alert )
                .setContentTitle( "Message from " + senderName )
                .setContentText( text )
                .setAutoCancel( true )
                .setContentIntent( pendingIntent );

        NotificationManager mgr = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
        mgr.notify( 0, builder.build() );
    }
}
