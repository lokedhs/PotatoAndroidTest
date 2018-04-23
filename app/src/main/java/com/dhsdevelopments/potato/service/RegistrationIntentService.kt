package com.dhsdevelopments.potato.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest
import com.dhsdevelopments.potato.common.DbTools
import com.dhsdevelopments.potato.common.Log
import com.google.firebase.iid.FirebaseInstanceId

class RegistrationIntentService : IntentService("RegistrationIntentService") {

    companion object {
        const val ACTION_REGISTER = "com.dhsdevelopments.potato.gcm.REGISTER"

        private const val PREFS_KEY_GCM_REGISTERED = "gcmRegisterOk"

        fun tokenUpdated(context: Context) {
            val updatedToken = FirebaseInstanceId.getInstance().token
            if(updatedToken != null) {
                val app = PotatoApplication.getInstance(context)
                if(app.isAuthenticated()) {
                    sendTokenToServer(context, updatedToken)
                }
            }
        }

        fun sendTokenToServer(context: Context, token: String) {
            val app = PotatoApplication.getInstance(context)
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            val call = app.findApiProvider().makePotatoApi().registerGcm(app.findApiKey(), GcmRegistrationRequest(token, "gcm"))
            val result = call.execute()
            if (!result.isSuccessful) {
                if (result.code() == 503) {
                    Log.w("GCM is disabled on the server")
                } else {
                    Log.e("Error when updating GCM key: " + result.code() + ", " + result.message())
                }
            } else if ("ok" == result.body()!!.result) {
                val prefsEditor = prefs.edit()
                prefsEditor.putBoolean(PREFS_KEY_GCM_REGISTERED, true)
                prefsEditor.apply()

                // If this was a new registration, we need to clear the channel notification configuration
                if (result.body()!!.detail == "token_registered") {
                    DbTools.clearUnreadNotificationSettings(context)
                }
            } else {
                Log.e("Unexpected reply from gcm registration: ${result.body()!!.result}")
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.action == ACTION_REGISTER) {
            handleRegister()
        }
    }

    private fun handleRegister() {
        tokenUpdated(this)
    }
}
