package com.dhsdevelopments.potato.wearable

import com.dhsdevelopments.potato.common.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageListenerService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("WatchMessageListener created")
    }

    override fun onDataChanged(events: DataEventBuffer) {
        Log.d("Got data change: $events")
        events.forEach { event ->
            Log.d("    type: ${event.type}, item: ${event.dataItem}")
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.d("Got message: $event")
    }
}
