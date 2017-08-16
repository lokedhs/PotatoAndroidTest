package com.dhsdevelopments.watch

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.util.concurrent.TimeUnit

class UpdateListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        Log.i("Got data changed: $events")

        val apiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build()
        val connectionResult = apiClient.blockingConnect(30, TimeUnit.SECONDS)
        if(!connectionResult.isSuccess) {
            Log.e("Unable to connect to api client")
            return
        }

        for(e in events) {
            Log.i("Processing event: $e")
        }
    }

}
