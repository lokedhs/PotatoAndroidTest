package com.dhsdevelopments.potato.messages;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest;
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import retrofit.Call;
import retrofit.Response;

import java.io.IOException;

public class RegistrationIntentService extends IntentService
{
    public static final String ACTION_REGISTER = "com.dhsdevelopments.potato.gcm.REGISTER";
    public final String PREFS_KEY_GCM_REGISTERED = "gcmRegisterOk";

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

            PotatoApplication app = PotatoApplication.getInstance( this );
            Call<GcmRegistrationResult> call = app.getPotatoApi().registerGcm( app.getApiKey(), new GcmRegistrationRequest( token ) );
            Response<GcmRegistrationResult> result = call.execute();
            if( !result.isSuccess() ) {
                if( result.code() == 503 ) {
                    Log.w( "GCM is disabled on the server" );
                }
                else {
                    Log.e( "Error when updating GCM key: " + result.code() + ", " + result.message() );
                }
            }
            else if( "ok".equals( result.body().result ) ) {
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putBoolean( PREFS_KEY_GCM_REGISTERED, true );
                prefsEditor.apply();
            }
        }
        catch( IOException e ) {
            Log.e( "Error when requesting token", e );
        }
    }
}
