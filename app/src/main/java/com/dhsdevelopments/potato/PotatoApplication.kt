package com.dhsdevelopments.potato

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.clientapi.ApiProvider
import com.dhsdevelopments.potato.clientapi.PotatoApi
import com.dhsdevelopments.potato.common.DateHelper
import com.dhsdevelopments.potato.common.makeRandomCharacterSequence
import com.dhsdevelopments.potato.imagecache.ImageCache

class PotatoApplication : Application() {

    val cacheDatabase: SQLiteDatabase by lazy { StorageHelper(this).writableDatabase }
    val apiProvider = ApiProvider(this)

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

    val apiKey: String
        get() = getPrefByName(R.string.pref_apikey)

    val userId: String
        get() = getPrefByName(R.string.pref_user_id)

    val sessionId = run {
        val buf = StringBuilder()
        makeRandomCharacterSequence(buf, 40)
        buf.toString()
    }

    companion object {
        private val IMAGE_CACHE_PURGE_CUTOFF_LONG = DateHelper.DAY_MILLIS
        private val IMAGE_CACHE_PURGE_CUTOFF_SHORT = DateHelper.HOUR_MILLIS

        fun getInstance(context: Context): PotatoApplication {
            return context.applicationContext as PotatoApplication
        }
    }
}
