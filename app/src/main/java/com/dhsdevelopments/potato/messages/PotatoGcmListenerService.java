package com.dhsdevelopments.potato.messages;

import android.os.Bundle;
import com.dhsdevelopments.potato.Log;
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
    }
}
