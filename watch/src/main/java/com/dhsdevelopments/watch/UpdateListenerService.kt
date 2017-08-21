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

    override fun onDataChanged(events: DataEventBuffer) {
        Log.d("Got data changed: $events")

        events
                .map { it.dataItem }
                .filter { it.uri.path == APIKEY_DATA_MAP_PATH }
                .forEach { updateCreds(it) }
    }

    private fun updateCreds(item: DataItem) {
        Log.d("Creds have been updated: $item")
    }

}
