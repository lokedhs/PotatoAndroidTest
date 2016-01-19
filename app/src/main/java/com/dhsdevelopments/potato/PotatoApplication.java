package com.dhsdevelopments.potato;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.dhsdevelopments.potato.clientapi.MessageElementTypeAdapter;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.dhsdevelopments.potato.clientapi.message.MessageElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import java.util.concurrent.TimeUnit;

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

    public PotatoApi getPotatoApi() {
        return makePotatoApi( -1 );
    }

    public PotatoApi getPotatoApiLongTimeout() {
        return makePotatoApi( 120 );
    }

    private PotatoApi makePotatoApi( int timeout ) {
        Gson gson = new GsonBuilder()
                            .setDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" )
                            .registerTypeAdapter( MessageElement.class, new MessageElementTypeAdapter() )
                            .create();
        OkHttpClient httpClient = new OkHttpClient();
        if( timeout > 0 ) {
            httpClient.setReadTimeout( timeout, TimeUnit.SECONDS );
        }
        Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl( "http://potato.dhsdevelopments.com/api/1.0/" )
                                    .addConverterFactory( GsonConverterFactory.create( gson ) )
                                    .client( httpClient )
                                    .build();
        return retrofit.create( PotatoApi.class );
    }
}
