package com.dhsdevelopments.potato.clientapi

import android.content.Context
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageElementTypeAdapter
import com.dhsdevelopments.potato.clientapi.notifications.NotificationTypeAdapter
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.common.R
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.GsonConverterFactory
import retrofit.Retrofit
import java.util.concurrent.TimeUnit

class ApiProvider(val context: Context) {

    val serverUrlPrefix by lazy {
        val identifier = context.resources.getIdentifier("com.dhsdevelopments.potato:string/override_server_prefix", null, null)
        Log.d("Got identifier for com.dhsdevelopments.potato:string/override_server_prefix = $identifier")
        val p = if (identifier == 0) {
            context.resources.getString(R.string.server_prefix)
        }
        else {
            context.resources.getString(identifier)
        }
        p ?: throw IllegalStateException("No server prefix found")
    }

    val apiUrlPrefix by lazy { serverUrlPrefix + "api/1.0/" }

    fun makePotatoApi(timeout: Int = -1): PotatoApi {
        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(MessageElement::class.java, MessageElementTypeAdapter())
                .registerTypeAdapter(PotatoNotification::class.java, NotificationTypeAdapter())
                .create()
        val httpClient = OkHttpClient()
        if (timeout > 0) {
            httpClient.setReadTimeout(timeout.toLong(), TimeUnit.SECONDS)
        }
        val retrofit = Retrofit.Builder()
                .baseUrl(apiUrlPrefix)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()
        return retrofit.create(PotatoApi::class.java)
    }

}
