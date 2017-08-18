package com.dhsdevelopments.potato.wearable

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_PATH
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_TOKEN
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_UID
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable


class WatchSendService : IntentService("WatchSendService") {

    companion object {
        val ACTION_SEND_API_KEY = "${WatchSendService::class.qualifiedName}.sendApiKey"

        fun sendApiKey(context: Context) {
            val intent = Intent(context, WatchSendService::class.java).apply {
                action = ACTION_SEND_API_KEY
            }
            context.startService(intent)
        }
    }

    private val connectionCallback = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(p0: Bundle?) {
            Log.d("API client connected: $p0")
        }

        override fun onConnectionSuspended(p0: Int) {
            Log.d("API client suspended: $p0")
        }
    }

    private val connectionFailedCallback = object : GoogleApiClient.OnConnectionFailedListener {
        override fun onConnectionFailed(result: ConnectionResult) {
            Log.d("API client connection failed: $result")
        }
    }

    override fun onHandleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_SEND_API_KEY -> updateWearableDataMap()
            else -> Log.e("Unexpected action: ${intent.action}")
        }
    }

    private fun updateWearableDataMap() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val token = prefs.getString(getString(R.string.pref_apikey), "")
        val uid = prefs.getString(getString(R.string.pref_user_id), "")

        if(token == "" || uid == "") {
            Log.w("No api key found. Will not update watch.")
            return
        }

        val apiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(connectionFailedCallback)
                .build()
        apiClient.blockingConnect()

        val putDataMapRequest = PutDataMapRequest.create(APIKEY_DATA_MAP_PATH).apply {
            dataMap.putString(APIKEY_DATA_MAP_TOKEN, token)
            dataMap.putString(APIKEY_DATA_MAP_UID, uid)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().apply {
            setUrgent()
        }
        Log.d("Sending api key information to watch: $putDataRequest, connected: ${apiClient.isConnected}, connecting: ${apiClient.isConnecting}")
        Wearable.DataApi.putDataItem(apiClient, putDataRequest).await()
        Log.d("  message sent, will disconnect")
        apiClient.disconnect()
    }

}
