package com.dhsdevelopments.potato.initial;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.LoginActivity;
import com.dhsdevelopments.potato.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

public class PotatoActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        String apiKey = prefs.getString( getString( R.string.pref_apikey ), "" );
        if( apiKey.equals( "" ) ) {
            Intent intent = new Intent( this, LoginActivity.class );
            startActivity( intent );
            finish();
        }
        else {
            Log.i( "got key: " + apiKey );
//            GoogleApiClient client = new GoogleApiClient.Builder( this )
//                    .addApi( Drive.API )
//                    .addOnConnectionFailedListener( this )
//                    .build();
//            client.connect();
            GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
            int result = availability.isGooglePlayServicesAvailable( this );
            if( result != ConnectionResult.SUCCESS ) {
                if( availability.isUserResolvableError( result ) ) {
                    Dialog dialog = availability.getErrorDialog( this, result, 0 );
                    dialog.show();
                }
                else {
                    throw new RuntimeException( "google apis not available" );
                }
            }
        }
    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult ) {
        Log.e( "Did not find the google apis" );
        throw new RuntimeException( "google apis not available" );
    }
}
