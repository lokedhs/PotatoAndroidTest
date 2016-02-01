package com.dhsdevelopments.potato.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.StorageHelper;
import com.dhsdevelopments.potato.clientapi.ClearNotificationsResult;
import com.dhsdevelopments.potato.clientapi.channel2.Channel;
import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult;
import com.dhsdevelopments.potato.clientapi.channel2.Domain;
import retrofit.Call;
import retrofit.Response;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RemoteRequestService extends IntentService
{
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_MARK_NOTIFICATIONS = "com.dhsdevelopments.potato.MARK_NOTIFICATIONS";
    public static final String ACTION_LOAD_CHANNEL_LIST = "com.dhsdevelopments.potato.LOAD_CHANNELS";
    public static final String EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id";

    public static final String ACTION_CHANNEL_LIST_UPDATED = "com.dhsdevelopments.potato.ACTION_CHANNEL_LIST_UPDATED";

    public static void markNotificationsForChannel( Context context, String cid ) {
        makeAndStartIntent( context, ACTION_MARK_NOTIFICATIONS, EXTRA_CHANNEL_ID, cid );
    }

    public static void loadChannelList( Context context ) {
        makeAndStartIntent( context, ACTION_LOAD_CHANNEL_LIST );
    }

    private static void makeAndStartIntent( Context context, String action, Object... extraElements ) {
        Intent intent = new Intent( context, RemoteRequestService.class );
        intent.setAction( action );
        for( int i = 0 ; i < extraElements.length ; i += 2 ) {
            String key = (String)extraElements[i];
            Object value = extraElements[i + 1];
            if( value instanceof String ) {
                intent.putExtra( key, (String)value );
            }
            else {
                throw new IllegalArgumentException( "Unexpected value type: " + value.getClass().getName() );
            }
        }
        context.startService( intent );
    }

    public RemoteRequestService() {
        super( "RemoteRequestService" );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        if( intent != null ) {
            switch( intent.getAction() ) {
                case ACTION_MARK_NOTIFICATIONS:
                    syncNotifications( intent.getStringExtra( EXTRA_CHANNEL_ID ) );
                    break;
                case ACTION_LOAD_CHANNEL_LIST:
                    loadChannels();
                    break;
            }
        }
    }

    private void syncNotifications( String cid ) {
        try {
            PotatoApplication app = PotatoApplication.getInstance( this );
            Call<ClearNotificationsResult> call = app.getPotatoApi().clearNotificationsForChannel( app.getApiKey(), cid );
            Response<ClearNotificationsResult> result = call.execute();
            if( result.isSuccess() ) {
                if( "ok".equals( result.body().result ) ) {
                    Log.d( "Notifications cleared for channel: " + cid );
                }
                else {
                    Log.e( "Unexpected result from notification clear request: " + result.body() );
                }
            }
            else {
                Log.e( "Unable to clear notifications on server: code=" + result.code() + ", message=" + result.message() );
            }
        }
        catch( IOException e ) {
            Log.e( "Exception when clearing notifications", e );
        }
    }

    private void loadChannels() {
        try {
            PotatoApplication app = PotatoApplication.getInstance( this );
            Call<ChannelsResult> call = app.getPotatoApi().getChannels2( app.getApiKey() );
            Response<ChannelsResult> result = call.execute();

            if( result.isSuccess() ) {
                SQLiteDatabase db = app.getCacheDatabase();
                db.beginTransaction();
                try {
                    // We need to delete everything from the table since this call returns the full state
                    db.delete( StorageHelper.CHANNELS_TABLE, null, null );
                    db.delete( StorageHelper.DOMAINS_TABLE, null, null );

                    for( Domain d : result.body().domains ) {
                        if( !d.type.equals( "PRIVATE" ) ) {
                            ContentValues values = new ContentValues();
                            values.put( StorageHelper.DOMAINS_ID, d.id );
                            values.put( StorageHelper.DOMAINS_NAME, d.name );
                            db.insert( StorageHelper.DOMAINS_TABLE, null, values );

                            for( Channel c : d.channels ) {
                                ContentValues channelValues = new ContentValues();
                                channelValues.put( StorageHelper.CHANNELS_ID, c.id );
                                channelValues.put( StorageHelper.CHANNELS_DOMAIN, d.id );
                                channelValues.put( StorageHelper.CHANNELS_NAME, c.name );
                                channelValues.put( StorageHelper.CHANNELS_UNREAD, c.unreadCount );
                                channelValues.put( StorageHelper.CHANNELS_PRIVATE, c.privateUser );
                                db.insert( StorageHelper.CHANNELS_TABLE, null, channelValues );
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }

                LocalBroadcastManager mgr = LocalBroadcastManager.getInstance( this );
                Intent intent = new Intent( ACTION_CHANNEL_LIST_UPDATED );
                mgr.sendBroadcast( intent );
            }
        }
        catch( IOException e ) {
            Log.e( "Exception when loading channels", e );
        }
    }
}
