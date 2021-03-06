package com.dhsdevelopments.potato.initial

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channellist.ChannelListActivity
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.login.WebLoginActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient

class PotatoActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    companion object {
        private const val AVAILABILITY_DIALOG_RESULT = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val serverName = prefs.getString(getString(R.string.pref_servername), "")
        if (serverName == "") {
            val intent = Intent(this, ReadServerNameActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val apiKey = prefs.getString(getString(R.string.pref_apikey), "")
            if (apiKey == "") {
                val intent = Intent(this, WebLoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val uid = prefs.getString(getString(R.string.pref_user_id), "")
                if (uid == "") {
                    throw RuntimeException("uid is not set in preferences, should probably look it up on the server here")
                }
                if (checkGooglePlayApis()) {
                    startChannelListActivity()
                }
            }
        }
    }

    private fun startChannelListActivity() {
        startActivity(Intent(this, ChannelListActivity::class.java))
        finish()
    }

    private fun checkGooglePlayApis(): Boolean {
        Log.d("Checking for google play apis")
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(this)
        Log.d("check result=$result")
        if (result != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(result)) {
                val dialog = availability.getErrorDialog(this, result, AVAILABILITY_DIALOG_RESULT)
                dialog.show()
            } else {
                throw RuntimeException("google apis not available")
            }
            return false
        } else {
            Log.w("Not calling registration directly, it should be called anyway")
//            val intent = Intent(this, RegistrationIntentService::class.java)
//            intent.action = RegistrationIntentService.ACTION_REGISTER
//            startService(intent)
            return true
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e("Did not find the google apis")
        throw RuntimeException("google apis not available")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AVAILABILITY_DIALOG_RESULT) {
            if (checkGooglePlayApis()) {
                startChannelListActivity()
            }
        }
    }
}
