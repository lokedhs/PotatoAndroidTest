package com.dhsdevelopments.potato.messages;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PotatoInstanceIDListenerService extends Service
{
    public PotatoInstanceIDListenerService() {
    }

    @Override
    public IBinder onBind( Intent intent ) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }
}
