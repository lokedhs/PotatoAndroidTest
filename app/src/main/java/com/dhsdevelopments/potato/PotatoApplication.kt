package com.dhsdevelopments.potato

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.clientapi.PotatoApi
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageElementTypeAdapter
import com.dhsdevelopments.potato.clientapi.notifications.NotificationTypeAdapter
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import java.util.concurrent.TimeUnit

class PotatoApplication : Application() {

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

    val sessionId = run {
        val buf = StringBuilder()
        makeRandomCharacterSequence(buf, 40)
        buf.toString()
    }

    private fun makePotatoApi(timeout: Int): PotatoApi {
        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(MessageElement::class.java, MessageElementTypeAdapter())
                .registerTypeAdapter(PotatoNotification::class.java, NotificationTypeAdapter())
                .create()
        val httpClient = OkHttpClient()
        if (timeout > 0) {
            httpClient.setReadTimeout(timeout.toLong(), TimeUnit.SECONDS)
        }
        val retrofit = Retrofit.Builder().baseUrl(apiUrlPrefix).addConverterFactory(GsonConverterFactory.create(gson)).client(httpClient).build()
        return retrofit.create(PotatoApi::class.java)
    }

    val serverUrlPrefix: String by lazy {
        val identifier = resources.getIdentifier("${PotatoApplication::class.java.`package`.name}:string/override_server_prefix", null, null)
        Log.d("Got identifier for ${PotatoApplication::class.java.`package`.name}:string/override_server_prefix = $identifier")
        if (identifier == 0) {
            resources.getString(R.string.server_prefix)
        }
        else {
            resources.getString(identifier)
        }
    }

    val apiUrlPrefix: String by lazy { serverUrlPrefix + "api/1.0/" }

    companion object {
        private val IMAGE_CACHE_PURGE_CUTOFF_LONG = DateHelper.DAY_MILLIS
        private val IMAGE_CACHE_PURGE_CUTOFF_SHORT = DateHelper.HOUR_MILLIS

        fun getInstance(context: Context): PotatoApplication {
            return context.applicationContext as PotatoApplication
        }
    }
}
