package com.dhsdevelopments.potato.service

import com.dhsdevelopments.potato.common.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class PotatoInstanceIDListenerService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        Log.d("Got token refresh message")
        val updatedToken = FirebaseInstanceId.getInstance().token
        if(updatedToken == null) {
            Log.e("Received null token from Firebase id listener")
        }
        else {
            RegistrationIntentService.sendTokenToServer(this, updatedToken)
        }
    }
}
