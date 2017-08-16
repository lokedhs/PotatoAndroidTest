package com.dhsdevelopments.watch

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_PATH
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_TOKEN
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_UID
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.util.concurrent.TimeUnit

class UpdateListenerService : WearableListenerService() {

    private lateinit var apiClient: GoogleApiClient

    override fun onCreate() {
        super.onCreate()
        apiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build()

        val connectionResult = apiClient.blockingConnect(30, TimeUnit.SECONDS)
        if(!connectionResult.isSuccess) {
            Log.e("Unable to connect to api client")
        }
    }

    override fun onDestroy() {
        apiClient.disconnect()
        super.onDestroy()
    }

    override fun onDataChanged(events: DataEventBuffer) {
        Log.i("Got data changed: $events")

        for(e in events) {
            val item = e.dataItem
            if(item.uri.path == APIKEY_DATA_MAP_PATH) {
                updateCreds(item)
            }
        }
    }

    private fun updateCreds(item: DataItem) {
        Log.i("Creds have been updated: $item")
    }

}
