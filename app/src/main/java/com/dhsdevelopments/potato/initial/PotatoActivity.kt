package com.dhsdevelopments.potato.initial

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channellist.ChannelListActivity
import com.dhsdevelopments.potato.login.WebLoginActivity
import com.dhsdevelopments.potato.service.RegistrationIntentService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient

class PotatoActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val apiKey = prefs.getString(getString(R.string.pref_apikey), "")
        if (apiKey == "") {
            val intent = Intent(this, WebLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            val uid = prefs.getString(getString(R.string.pref_user_id), "")
            if (uid == "") {
                throw RuntimeException("uid is not set in preferences, should probably look it up on the server here")
            }
            checkGooglePlayApis()
            startActivity(Intent(this, ChannelListActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun checkGooglePlayApis() {
        com.dhsdevelopments.potato.common.Log.d("Checking for google play apis")
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(this)
        com.dhsdevelopments.potato.common.Log.d("check result=$result")
        if (result != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(result)) {
                val dialog = availability.getErrorDialog(this, result, 0)
                dialog.show()
            }
            else {
                throw RuntimeException("google apis not available")
            }
        }
        else {
            val intent = Intent(this, RegistrationIntentService::class.java)
            intent.action = RegistrationIntentService.ACTION_REGISTER
            startService(intent)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        com.dhsdevelopments.potato.common.Log.e("Did not find the google apis")
        throw RuntimeException("google apis not available")
    }
}
