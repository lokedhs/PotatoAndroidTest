package com.dhsdevelopments.potato.messages;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService
{
    public static final String ACTION_REGISTER = "com.dhsdevelopments.potato.gcm.REGISTER";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RegistrationIntentService() {
        super( "RegistrationIntentService" );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
        if( intent != null && intent.getAction().equals( ACTION_REGISTER ) ) {
            handleRegister();
        }
    }

    private void handleRegister() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );

            InstanceID instanceId = InstanceID.getInstance( this );
            String token = instanceId.getToken( getString( R.string.gcm_sender_id ), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null );

            Log.d( "Got token: " + token );
        }
        catch( IOException e ) {
            Log.e( "Error when requesting token", e );
        }
    }

}
