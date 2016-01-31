package com.dhsdevelopments.potato;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.dhsdevelopments.potato.clientapi.MessageElementTypeAdapter;
import com.dhsdevelopments.potato.clientapi.NotificationTypeAdapter;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.dhsdevelopments.potato.clientapi.message.MessageElement;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification;
import com.dhsdevelopments.potato.imagecache.ImageCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import java.util.concurrent.TimeUnit;

public class PotatoApplication extends Application
{
    public static final String SERVER_URL_PREFIX = "http://10.0.2.2:8080/";
    //public static final String SERVER_URL_PREFIX = "http://potato.dhsdevelopments.com/";
    public static final String API_URL_PREFIX = SERVER_URL_PREFIX + "api/1.0";

    private static final long IMAGE_CACHE_PURGE_CUTOFF_LONG = DateHelper.DAY_MILLIS;
    private static final long IMAGE_CACHE_PURGE_CUTOFF_SHORT = DateHelper.HOUR_MILLIS;

    public static final String PREF_DEFAULT_DOMAIN_ID = "com.dhsdevelopments.potato.PREF_DEFAULT_DOMAIN";

    private SQLiteDatabase cacheDatabase = null;

    @Override
    public void onCreate() {
        super.onCreate();

        ImageCache imageCache = new ImageCache( this );
        imageCache.purge( IMAGE_CACHE_PURGE_CUTOFF_LONG, IMAGE_CACHE_PURGE_CUTOFF_SHORT );
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
                            .registerTypeAdapter( PotatoNotification.class, new NotificationTypeAdapter() )
                            .create();
        OkHttpClient httpClient = new OkHttpClient();
        if( timeout > 0 ) {
            httpClient.setReadTimeout( timeout, TimeUnit.SECONDS );
        }
        Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl( PotatoApplication.API_URL_PREFIX + "/" )
                                    .addConverterFactory( GsonConverterFactory.create( gson ) )
                                    .client( httpClient )
                                    .build();
        return retrofit.create( PotatoApi.class );
    }

    public SQLiteDatabase getCacheDatabase() {
        if( cacheDatabase == null ) {
            StorageHelper storageHelper = new StorageHelper( this );
            cacheDatabase = storageHelper.getWritableDatabase();
        }
        return cacheDatabase;
    }
}
