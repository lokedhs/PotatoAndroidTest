package com.dhsdevelopments.watch

import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_PATH
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class UpdateListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        Log.d("Got data changed: $events")

        events
                .map { it.dataItem }
                .filter { it.uri.path == APIKEY_DATA_MAP_PATH }
                .forEach { updateCreds(it) }
    }

    override fun onMessageReceived(message: MessageEvent) {
        Log.d("Got message event: $message")
    }

    private fun updateCreds(item: DataItem) {
        Log.d("Creds have been updated: $item")
    }

}
