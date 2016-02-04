package com.dhsdevelopments.potato.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.StorageHelper;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity;
import com.dhsdevelopments.potato.channelmessages.ChannelContentFragment;
import com.google.android.gms.gcm.GcmListenerService;

public class PotatoGcmListenerService extends GcmListenerService
{
    private static final String UNREAD_NOTIFICATIONS_TAG = "unread_channels";
    private static final int MESSAGE_NOTIFICATION_ID = 0;
    private static final int UNREAD_NOTIFICATION_ID = 1;

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

        String messageType = data.getString( "potato_message_type" );
        if( messageType == null ) {
            Log.e( "Missing message_type in notification" );
        }
        else {
            switch( messageType ) {
                case "message":
                    processMessage( data );
                    break;
                case "unread":
                    processUnread( data );
                    break;
            }
        }
    }

    private void processMessage( Bundle data ) {
        String messageId = data.getString( "message_id" );
        String notificationType = data.getString( "notification_type" );
        String text = data.getString( "text" );
        String senderId = data.getString( "sender_id" );
        String senderName = data.getString( "sender_name" );
        String channelId = data.getString( "channel" );

        Intent intent = new Intent( this, ChannelContentActivity.class );
        intent.putExtra( ChannelContentFragment.ARG_CHANNEL_ID, channelId );
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_ONE_SHOT );

        NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder( this )
                                                     .setSmallIcon( android.R.drawable.ic_dialog_alert )
                                                     .setContentTitle( "Message from " + senderName )
                                                     .setContentText( text )
                                                     .setAutoCancel( true )
                                                     .setContentIntent( pendingIntent );

        NotificationManager mgr = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
        mgr.notify( MESSAGE_NOTIFICATION_ID, builder.build() );
    }

    private void processUnread( Bundle data ) {
        String cid = data.getString( "channel" );
        int unreadCount = Integer.parseInt( data.getString( "unread" ) );
        Log.d( "Got unread notification: cid=" + cid + ", unreadCount=" + unreadCount );

        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();
        ContentValues values = new ContentValues();
        values.put( StorageHelper.CHANNELS_UNREAD, unreadCount );
        int res = db.update( StorageHelper.CHANNELS_TABLE, values, StorageHelper.CHANNELS_ID + " = ?", new String[] { cid } );
        if( res > 0 ) {
            sendUnreadNotification( db );
        }
    }

    private void sendUnreadNotification( SQLiteDatabase db ) {
        try( Cursor result = db.query( StorageHelper.CHANNELS_TABLE,
                                       new String[] { "count(*)" },
                                       StorageHelper.CHANNELS_UNREAD + " > ?", new String[] { "0" },
                                       null, null, null, null ) ) {
            if( !result.moveToNext() ) {
                Log.e( "No result when loading number of unrad channels" );
                return;
            }
            int unread = result.getInt( 0 );
            NotificationManager mgr = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
            if( unread == 0 ) {
                mgr.cancel( UNREAD_NOTIFICATIONS_TAG, UNREAD_NOTIFICATION_ID );
            }
            else {
                Intent intent = new Intent( this, ChannelListActivity.class );
                PendingIntent pendingIntent = PendingIntent.getActivity( this, 1, intent, PendingIntent.FLAG_ONE_SHOT );
                NotificationCompat.Builder builder = new NotificationCompat.Builder( this )
                                                             .setSmallIcon( android.R.drawable.ic_dialog_email )
                                                             .setContentTitle( "New Potato messages" )
                                                             .setContentText( "You have new messages in " + unread + " channel" + (unread == 1 ? "" : "s") )
                                                             .setAutoCancel( true )
                                                             .setContentIntent( pendingIntent );
                mgr.notify( UNREAD_NOTIFICATIONS_TAG, UNREAD_NOTIFICATION_ID, builder.build() );
            }
        }
    }
}
