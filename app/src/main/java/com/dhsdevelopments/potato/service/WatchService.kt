package com.dhsdevelopments.potato.service

import com.dhsdevelopments.potato.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService

class WatchService() : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer?) {
        Log.i("Got message: $events")
    }
}
