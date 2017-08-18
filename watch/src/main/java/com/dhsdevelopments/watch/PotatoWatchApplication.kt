package com.dhsdevelopments.watch

import android.app.Application
import android.content.Context
import com.dhsdevelopments.potato.clientapi.ApiProvider

class PotatoWatchApplication : Application() {

    val apiProvider = ApiProvider(this)

    companion object {
        fun getInstance(context: Context): PotatoWatchApplication {
            return context.applicationContext as PotatoWatchApplication
        }
    }

}
