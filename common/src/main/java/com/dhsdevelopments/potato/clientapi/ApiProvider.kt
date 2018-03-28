package com.dhsdevelopments.potato.clientapi

import android.content.Context
import android.preference.PreferenceManager
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageElementTypeAdapter
import com.dhsdevelopments.potato.clientapi.notifications.NotificationTypeAdapter
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.common.R
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiProvider(val context: Context) {

    val serverUrlPrefix by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val serverName = prefs.getString(context.getString(R.string.pref_servername), "")!!
        if (serverName == "") {
            throw IllegalStateException("No server prefix found")
        }
        serverName
    }

    val apiUrlPrefix: String
        get() = makeApiUrlPrefix(serverUrlPrefix)

    fun makeApiUrlPrefix(url: String) = "${url}api/1.0/"

    fun makePotatoApi(timeout: Long = -1, urlPrefix: String = serverUrlPrefix): PotatoApi {
        Log.i("Creating api with timeout $timeout")
        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(MessageElement::class.java, MessageElementTypeAdapter())
                .registerTypeAdapter(PotatoNotification::class.java, NotificationTypeAdapter())
                .create()
        val builder = OkHttpClient.Builder()
        if (timeout > 0) {
            builder.readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .build()
        }
        val httpClient = builder.build()
        val retrofit = Retrofit.Builder()
                .baseUrl(makeApiUrlPrefix(urlPrefix))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()
        return retrofit.create(PotatoApi::class.java)
    }

}
