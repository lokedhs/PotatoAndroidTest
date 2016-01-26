package com.dhsdevelopments.potato.messages;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.dhsdevelopments.potato.Log;
import com.google.android.gms.iid.InstanceIDListenerService;

public class PotatoInstanceIDListenerService extends InstanceIDListenerService
{
    @Override
    public void onTokenRefresh() {
        Log.d( "Got token refresh message" );

        Intent intent = new Intent( this, RegistrationIntentService.class );
        intent.setAction( RegistrationIntentService.ACTION_REGISTER );
        startService( intent );
    }
}
