package com.dhsdevelopments.potato;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PotatoApplication extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static PotatoApplication getInstance( Context context ) {
        return (PotatoApplication)context.getApplicationContext();
    }

    public String getApiKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        String apiKey = prefs.getString( getString( R.string.pref_apikey ), "" );
        if( apiKey.equals( "" ) ) {
            throw new RuntimeException( "API key not configured" );
        }
        return apiKey;
    }
}
