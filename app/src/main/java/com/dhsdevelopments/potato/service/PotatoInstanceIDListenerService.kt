package com.dhsdevelopments.potato.service

import android.content.Intent
import com.dhsdevelopments.potato.Log
import com.google.android.gms.iid.InstanceIDListenerService

class PotatoInstanceIDListenerService : InstanceIDListenerService() {
    override fun onTokenRefresh() {
        Log.d("Got token refresh message")

        val intent = Intent(this, RegistrationIntentService::class.java)
        intent.action = RegistrationIntentService.ACTION_REGISTER
        startService(intent)
    }
}
