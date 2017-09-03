package com.dhsdevelopments.watch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import com.dhsdevelopments.potato.clientapi.ApiProvider
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_PATH
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_TOKEN
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_UID
import com.dhsdevelopments.potato.common.CommonApplication
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

class PotatoWatchApplication : CommonApplication() {

    data class UserData(val userId: String, val apiKey: String)

    private val PREF_KEY_USER_ID = "userId"
    private val PREF_KEY_API_KEY = "apiKey"

    private val apiProvider = ApiProvider(this)
    var userData: UserData? = null

    companion object {
        val ACTION_USER_DATA_UPDATED = PotatoWatchApplication::class.qualifiedName + ".userDataUpdated"

        fun getInstance(context: Context): PotatoWatchApplication {
            return context.applicationContext as PotatoWatchApplication
        }
    }

    override fun findApiProvider() = apiProvider

    private fun userDataOrError() = userData ?: throw IllegalStateException("API key not found")

    override fun findApiKey() = userDataOrError().apiKey
    override fun findUserId() = userDataOrError().userId

    val hasUserData get() = userData != null

    override fun onCreate() {
        super.onCreate()
        updateUserData()
    }

    private fun updateUserData() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val userId = prefs.getString(PREF_KEY_USER_ID, "")
        val apiKey = prefs.getString(PREF_KEY_API_KEY, "")
        if (userId == "" || apiKey == "") {
            updateUserDataFromDataMap()
        }
        else {
            userData = UserData(userId, apiKey)
        }
    }

    private fun saveUserDataToPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val p = prefs.edit().apply {
            putString(PREF_KEY_USER_ID, userData?.userId ?: "")
            putString(PREF_KEY_API_KEY, userData?.apiKey ?: "")
        }
        p.apply()
    }

    private fun sendUserDataUpdated() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_USER_DATA_UPDATED))
    }

    private fun updateUserDataFromDataMap() {
        var apiClient: GoogleApiClient? = null
        apiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnectionSuspended(p0: Int) {
                    }

                    override fun onConnected(p0: Bundle?) {
                        val url = Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(APIKEY_DATA_MAP_PATH).build()
                        Wearable.DataApi.getDataItems(apiClient, url)
                                .setResultCallback { items ->
                                    if (items.status.isSuccess && items.count > 0) {
                                        // There could possibly be more than one API key data entry available.
                                        // We're just using the first one, but that might not be the best solution.
                                        val m = DataMap.fromByteArray(items[0].data)
                                        userData = UserData(m.get(APIKEY_DATA_MAP_UID), m.get(APIKEY_DATA_MAP_TOKEN))
                                        saveUserDataToPrefs()
                                        sendUserDataUpdated()
                                    }
                                    items.release()
                                    apiClient!!.disconnect()
                                }
                    }
                }).build()
        apiClient.connect()
    }
}
