package com.dhsdevelopments.potato

import android.content.Context
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.clientapi.ApiProvider
import com.dhsdevelopments.potato.common.CommonApplication
import com.dhsdevelopments.potato.common.DateHelper
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.imagecache.makeImagesCacheDb

class PotatoApplication : CommonApplication() {

    private val apiProvider = ApiProvider(this)

    val imageCacheDb by lazy { makeImagesCacheDb(this) }

    override fun onCreate() {
        super.onCreate()

        val imageCache = ImageCache(this)
        imageCache.purge(IMAGE_CACHE_PURGE_CUTOFF_LONG, IMAGE_CACHE_PURGE_CUTOFF_SHORT)
    }

    private fun getPrefByName(name: Int): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val prefName = getString(name)
        val value = prefs.getString(prefName, "")
        if (value == "") {
            throw RuntimeException("Preference value not found: " + prefName)
        }
        return value
    }

    override fun findApiProvider() = apiProvider

    override fun findApiKey(): String = getPrefByName(R.string.pref_apikey)
    override fun findUserId(): String = getPrefByName(R.string.pref_user_id)

    companion object {
        private val IMAGE_CACHE_PURGE_CUTOFF_LONG = DateHelper.DAY_MILLIS
        private val IMAGE_CACHE_PURGE_CUTOFF_SHORT = DateHelper.HOUR_MILLIS

        fun getInstance(context: Context): PotatoApplication {
            return context.applicationContext as PotatoApplication
        }
    }
}
