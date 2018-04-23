package com.dhsdevelopments.potato.service

import com.google.firebase.iid.FirebaseInstanceIdService

class PotatoInstanceIDListenerService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        RegistrationIntentService.tokenUpdated(this)
    }
}
