package com.dhsdevelopments.potato.wearable

import com.dhsdevelopments.potato.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        Log.i("Got data change: $events")
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.i("Got message: $event")
    }
}
