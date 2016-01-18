package com.dhsdevelopments.potato.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import com.dhsdevelopments.potato.Log;

public class PotatoService extends Service
{
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    public PotatoService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread( "PotatoService", android.os.Process.THREAD_PRIORITY_BACKGROUND );
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler( serviceLooper );
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.i( "Starting potato service" );
        Message msg = serviceHandler.obtainMessage();
        // TODO: Add message data here
        serviceHandler.sendMessage( msg );
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i( "Destroying potato service" );
        super.onDestroy();
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    private class ServiceHandler extends Handler
    {
        public ServiceHandler( Looper serviceLooper ) {
            super( serviceLooper );
        }
    }
}
