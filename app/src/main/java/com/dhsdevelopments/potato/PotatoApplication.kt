package com.dhsdevelopments.potato

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.support.multidex.MultiDexApplication
import com.dhsdevelopments.potato.clientapi.MessageElementTypeAdapter
import com.dhsdevelopments.potato.clientapi.NotificationTypeAdapter
import com.dhsdevelopments.potato.clientapi.PotatoApi
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import java.util.concurrent.TimeUnit

class PotatoApplication : MultiDexApplication() {

    val cacheDatabase: SQLiteDatabase by lazy { StorageHelper(this).writableDatabase }

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

    val potatoApi: PotatoApi
        get() = makePotatoApi(-1)

    val potatoApiLongTimeout: PotatoApi
        get() = makePotatoApi(120)

    private fun makePotatoApi(timeout: Int): PotatoApi {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").registerTypeAdapter(MessageElement::class.java, MessageElementTypeAdapter()).registerTypeAdapter(PotatoNotification::class.java, NotificationTypeAdapter()).create()
        val httpClient = OkHttpClient()
        if (timeout > 0) {
            httpClient.setReadTimeout(timeout.toLong(), TimeUnit.SECONDS)
        }
        val retrofit = Retrofit.Builder().baseUrl(PotatoApplication.API_URL_PREFIX + "/").addConverterFactory(GsonConverterFactory.create(gson)).client(httpClient).build()
        return retrofit.create(PotatoApi::class.java)
    }

    companion object {
        @JvmField
        //val SERVER_URL_PREFIX = "http://10.0.2.2:8080/"
        val SERVER_URL_PREFIX = "http://potato.dhsdevelopments.com/"
        @JvmField
        val API_URL_PREFIX = SERVER_URL_PREFIX + "api/1.0"

        private val IMAGE_CACHE_PURGE_CUTOFF_LONG = DateHelper.DAY_MILLIS
        private val IMAGE_CACHE_PURGE_CUTOFF_SHORT = DateHelper.HOUR_MILLIS

        fun getInstance(context: Context): PotatoApplication {
            return context.applicationContext as PotatoApplication
        }
    }
}
