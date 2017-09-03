package com.dhsdevelopments.potato.service

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest
import com.dhsdevelopments.potato.common.DbTools
import com.dhsdevelopments.potato.common.Log
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import java.io.IOException

class RegistrationIntentService : IntentService("RegistrationIntentService") {

    val PREFS_KEY_GCM_REGISTERED = "gcmRegisterOk"

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.action == ACTION_REGISTER) {
            handleRegister()
        }
    }

    private fun handleRegister() {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)

            val instanceId = InstanceID.getInstance(this)
            val token = instanceId.getToken(getString(R.string.gcm_sender_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)

            Log.d("Got token: $token")

            val app = PotatoApplication.getInstance(this)
            val call = app.findApiProvider().makePotatoApi().registerGcm(app.findApiKey(), GcmRegistrationRequest(token, "gcm"))
            val result = call.execute()
            if (!result.isSuccess) {
                if (result.code() == 503) {
                    Log.w("GCM is disabled on the server")
                }
                else {
                    Log.e("Error when updating GCM key: " + result.code() + ", " + result.message())
                }
            }
            else if ("ok" == result.body().result) {
                val prefsEditor = prefs.edit()
                prefsEditor.putBoolean(PREFS_KEY_GCM_REGISTERED, true)
                prefsEditor.apply()

                // If this was a new registration, we need to clear the channel notification configuration
                if ("token_registered" == result.body().detail) {
                    DbTools.clearUnreadNotificationSettings(this)
                }
            }
            else {
                Log.e("Unexpected reply from gcm registration: ${result.body().result}")
            }
        }
        catch (e: IOException) {
            Log.e("Error when requesting token", e)
        }

    }

    companion object {
        val ACTION_REGISTER = "com.dhsdevelopments.potato.gcm.REGISTER"
    }
}
