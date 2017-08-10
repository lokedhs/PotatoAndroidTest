package com.dhsdevelopments.watch

import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class WatchTestActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_test)

        // Enables Always-on
        setAmbientEnabled()
    }
}
