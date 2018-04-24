package com.dhsdevelopments.potato

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.clientapi.ApiProvider
import com.dhsdevelopments.potato.common.CommonApplication
import com.dhsdevelopments.potato.common.DateHelper
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.imagecache.makeImagesCacheDb

class PotatoApplication : CommonApplication() {

    private val apiProvider = ApiProvider(this)

    val imageCacheDb by lazy { makeImagesCacheDb(this) }
    lateinit var notificationChannel: NotificationChannel

    override fun onCreate() {
        super.onCreate()

        val imageCache = ImageCache(this)
        imageCache.purge(IMAGE_CACHE_PURGE_CUTOFF_LONG, IMAGE_CACHE_PURGE_CUTOFF_SHORT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(NotificationChannel(NOTIFICATION_CHANNEL_ID, "Potato", NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    private fun getPrefByName(name: Int, allowEmpty: Boolean = false): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val prefName = getString(name)
        val value = prefs.getString(prefName, "")
        if (value == "" && !allowEmpty) {
            throw RuntimeException("Preference value not found: $prefName")
        }
        return value
    }

    override fun findApiProvider() = apiProvider
    override fun findApiKey(): String = getPrefByName(R.string.pref_apikey)
    override fun isAuthenticated(): Boolean = getPrefByName(R.string.pref_apikey, true) != ""

    override fun findGcmSenderId(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString(getString(R.string.pref_gcm_sender), "")
    }

    override fun findUserId(): String = getPrefByName(R.string.pref_user_id)

    companion object {
        private const val IMAGE_CACHE_PURGE_CUTOFF_LONG = DateHelper.DAY_MILLIS
        private const val IMAGE_CACHE_PURGE_CUTOFF_SHORT = DateHelper.HOUR_MILLIS

        val NOTIFICATION_CHANNEL_ID = "potato_channel"

        fun getInstance(context: Context): PotatoApplication {
            return context.applicationContext as PotatoApplication
        }
    }
}
