package com.dhsdevelopments.potato.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.clientapi.ClearNotificationsResult;
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
    public static final String ACTION_REQUEST_DOMAINS = "com.dhsdevelopments.potato.REQUEST_DOMAINS";
    public static final String EXTRA_CHANNEL_ID = "channelId";

    public static void markNotificationsForChannel( Context context, String cid ) {
        makeAndStartIntent( context, ACTION_MARK_NOTIFICATIONS, EXTRA_CHANNEL_ID, cid );
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
}
