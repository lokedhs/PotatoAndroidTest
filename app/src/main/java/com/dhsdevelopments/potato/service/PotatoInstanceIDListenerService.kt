package com.dhsdevelopments.potato.service

import android.content.Intent
import com.google.android.gms.iid.InstanceIDListenerService

class PotatoInstanceIDListenerService : InstanceIDListenerService() {
    override fun onTokenRefresh() {
        com.dhsdevelopments.potato.common.Log.d("Got token refresh message")

        val intent = Intent(this, RegistrationIntentService::class.java)
        intent.action = RegistrationIntentService.ACTION_REGISTER
        startService(intent)
    }
}
