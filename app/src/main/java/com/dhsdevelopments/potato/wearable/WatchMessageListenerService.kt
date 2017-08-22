package com.dhsdevelopments.potato.wearable

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageListenerService : WearableListenerService() {
    override fun onCreate() {
        super.onCreate()
        com.dhsdevelopments.potato.common.Log.d("WatchMessageListener created")
    }

    override fun onDataChanged(events: DataEventBuffer) {
        com.dhsdevelopments.potato.common.Log.d("Got data change: $events")
        events.forEach { event ->
            com.dhsdevelopments.potato.common.Log.d("    type: ${event.type}, item: ${event.dataItem}")
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        com.dhsdevelopments.potato.common.Log.d("Got message: $event")
    }
}
