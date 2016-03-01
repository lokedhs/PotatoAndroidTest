package com.dhsdevelopments.potato

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

@Suppress("ConvertToStringTemplate")
class StorageHelper(context: Context) : SQLiteOpenHelper(context, "potatoData", null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createImageCacheTables(db)
        createChannelTables(db)
        createChannelConfigTable(db)
    }

    private fun createImageCacheTables(db: SQLiteDatabase) {
        db.execSQL("create table $IMAGE_CACHE_TABLE (" +
                "$IMAGE_CACHE_NAME text primary key, " +
                "$IMAGE_CACHE_FILENAME text, " +
                "$IMAGE_CACHE_CREATED_DATE int not null, " +
                "$IMAGE_CACHE_IMAGE_AVAILABLE boolean, " +
                "$IMAGE_CACHE_CAN_DELETE boolean)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    private fun createChannelTables(db: SQLiteDatabase) {
        db.execSQL("create table $DOMAINS_TABLE (" +
                "$DOMAINS_ID text primary key, " +
                "$DOMAINS_NAME text not null)")
        db.execSQL("create table $CHANNELS_TABLE (" +
                "$CHANNELS_ID text primary key, " +
                "$CHANNELS_DOMAIN text not null, " +
                "$CHANNELS_NAME text not null, " +
                "$CHANNELS_UNREAD text not null, " +
                "$CHANNELS_PRIVATE text null, " +
                "foreign key (domain) references $DOMAINS_TABLE($DOMAINS_ID))")
    }

    private fun createChannelConfigTable(db: SQLiteDatabase) {
        db.execSQL("create table $CHANNEL_CONFIG_TABLE (" +
                "$CHANNEL_CONFIG_ID text primary key, " +
                "$CHANNEL_CONFIG_SHOW_NOTIFICATIONS boolean not null, " +
                "$CHANNEL_CONFIG_NOTIFY_UNREAD boolean not null)")
    }

    companion object {
        private val DATABASE_VERSION = 3

        val IMAGE_CACHE_TABLE = "imagecache"
        val IMAGE_CACHE_NAME = "name"
        val IMAGE_CACHE_FILENAME = "filename"
        val IMAGE_CACHE_CREATED_DATE = "createdDate"
        val IMAGE_CACHE_IMAGE_AVAILABLE = "imageAvailable"
        val IMAGE_CACHE_CAN_DELETE = "canDelete"

        val DOMAINS_TABLE = "domains"
        val DOMAINS_ID = "id"
        val DOMAINS_NAME = "name"

        val CHANNELS_TABLE = "channels"
        val CHANNELS_ID = "id"
        val CHANNELS_DOMAIN = "domain"
        val CHANNELS_NAME = "name"
        val CHANNELS_UNREAD = "unread"
        val CHANNELS_PRIVATE = "private_user"

        val CHANNEL_CONFIG_TABLE = "channel_config"
        val CHANNEL_CONFIG_ID = "id"
        val CHANNEL_CONFIG_SHOW_NOTIFICATIONS = "show_notification"
        val CHANNEL_CONFIG_NOTIFY_UNREAD = "show_unread"
    }
}

fun loadChannelConfigFromDb(db: SQLiteDatabase, channelId: String): Cursor {
    return db.query(StorageHelper.CHANNEL_CONFIG_TABLE,
            arrayOf(StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD),
            "${StorageHelper.CHANNEL_CONFIG_ID} = ?", arrayOf(channelId),
            null, null, null, null)
}

fun loadDomainsFromDb(context: Context): List<DomainDescriptor> {
    val domains = ArrayList<DomainDescriptor>()
    PotatoApplication.getInstance(context).cacheDatabase.query(StorageHelper.DOMAINS_TABLE,
            arrayOf(StorageHelper.DOMAINS_ID, StorageHelper.DOMAINS_NAME),
            null, null, null, null, null, null).use { result ->
        while (result.moveToNext()) {
            val id = result.getString(0)
            val name = result.getString(1)
            domains.add(DomainDescriptor(id, name))
        }
    }
    return domains
}

fun loadAllChannelIdsInDomain(context: Context, domainId: String): Set<String> {
    val channels = HashSet<String>()
    PotatoApplication.getInstance(context).cacheDatabase.query(StorageHelper.CHANNELS_TABLE,
            arrayOf(StorageHelper.CHANNELS_ID),
            "${StorageHelper.CHANNELS_DOMAIN} = ?", arrayOf(domainId),
            null, null, null).use { result ->
        while (result.moveToNext()) {
            channels.add(result.getString(0))
        }
    }
    return channels
}

fun isChannelJoined(context: Context, cid: String): Boolean {
    return PotatoApplication.getInstance(context).cacheDatabase.query(StorageHelper.CHANNELS_TABLE,
            arrayOf(StorageHelper.CHANNELS_ID),
            "${StorageHelper.CHANNELS_ID} = ?", arrayOf(cid),
            null, null, null).use { result ->
        result.moveToNext()
    }
}

class DomainDescriptor(val id: String, val name: String)
