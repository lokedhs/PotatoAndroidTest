package com.dhsdevelopments.potato.common

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dhsdevelopments.potato.clientapi.ApiProvider

abstract class CommonApplication : Application() {

    val cacheDatabase by lazy { DbTools.makePotatoDb(this) }

    val sessionId = run {
        val buf = StringBuilder()
        makeRandomCharacterSequence(buf, 40)
        buf.toString()
    }

    abstract fun findApiProvider(): ApiProvider

    abstract fun findApiKey(): String
    abstract fun findUserId(): String

    companion object {
        fun getInstance(context: Context) = context.applicationContext as CommonApplication
    }

}
