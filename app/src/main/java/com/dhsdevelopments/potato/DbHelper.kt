package com.dhsdevelopments.potato

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

fun queryForChannel(db: SQLiteDatabase, channelId: String): Cursor {
    return db.query(StorageHelper.CHANNEL_CONFIG_TABLE,
            arrayOf(StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD),
            StorageHelper.CHANNEL_CONFIG_ID + " = ?", arrayOf(channelId),
            null, null, null, null)
}
