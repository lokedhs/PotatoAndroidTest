package com.dhsdevelopments.potato.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.dhsdevelopments.potato.StorageHelper

fun queryForChannel(db: SQLiteDatabase, channelId: String): Cursor {
    return db.query(StorageHelper.CHANNEL_CONFIG_TABLE,
            arrayOf(StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD),
            "${StorageHelper.CHANNEL_CONFIG_ID} = ?", arrayOf(channelId),
            null, null, null, null)
}
