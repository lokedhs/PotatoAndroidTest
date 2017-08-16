package com.dhsdevelopments.potato

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dhsdevelopments.potato.clientapi.callServiceBackground
import com.dhsdevelopments.potato.clientapi.channelinfo.LoadChannelInfoResult
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.internal.zzbi
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
                "$CHANNELS_HIDDEN int not null, " +
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
        val CHANNELS_HIDDEN = "hidden"

        val CHANNEL_CONFIG_TABLE = "channel_config"
        val CHANNEL_CONFIG_ID = "id"
        val CHANNEL_CONFIG_SHOW_NOTIFICATIONS = "show_notification"
        val CHANNEL_CONFIG_NOTIFY_UNREAD = "show_unread"
    }
}

class ChannelDescriptor(val id: String, val name: String, val privateUser: String?, val hidden: Boolean, val domainId: String)
class DomainDescriptor(val id: String, val name: String)

object DbTools {

    fun loadChannelInfoFromDb(context: Context, cid: String): ChannelDescriptor {
        return PotatoApplication.getInstance(context).cacheDatabase.query(StorageHelper.CHANNELS_TABLE,
                arrayOf(StorageHelper.CHANNELS_ID,
                        StorageHelper.CHANNELS_NAME,
                        StorageHelper.CHANNELS_PRIVATE,
                        StorageHelper.CHANNELS_HIDDEN,
                        StorageHelper.CHANNELS_DOMAIN),
                "${StorageHelper.CHANNELS_ID} = ?", arrayOf(cid),
                null, null, null).use { result ->
            if (!result.moveToNext()) {
                throw IllegalStateException("Channel not found in database: $cid")
            }
            ChannelDescriptor(result.getString(0), result.getString(1), result.getString(2), result.getInt(3) != 0, result.getString(4))
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

    fun insertChannelIntoChannelsTable(db: SQLiteDatabase, channelId: String, domainId: String, name: String, unreadCount: Int, privateUser: String?, hide: Boolean) {
        val channelValues = ContentValues()
        channelValues.put(StorageHelper.CHANNELS_ID, channelId)
        channelValues.put(StorageHelper.CHANNELS_DOMAIN, domainId)
        channelValues.put(StorageHelper.CHANNELS_NAME, name)
        channelValues.put(StorageHelper.CHANNELS_UNREAD, unreadCount)
        channelValues.put(StorageHelper.CHANNELS_PRIVATE, privateUser)
        channelValues.put(StorageHelper.CHANNELS_HIDDEN, hide)
        db.insert(StorageHelper.CHANNELS_TABLE, null, channelValues)
    }

    fun ensureChannelInfo(context: Context, cid: String, fn: () -> Unit) {
        val db = PotatoApplication.getInstance(context)
        val exists = db.cacheDatabase.query(StorageHelper.CHANNELS_TABLE, arrayOf(StorageHelper.CHANNELS_ID),
                "${StorageHelper.CHANNELS_ID} = ?", arrayOf(cid),
                null, null, null).use {
            it.moveToNext()
        }

        if (exists) {
            fn()
        }
        else {
            refreshChannelEntryInDb(context, cid,
                    { message -> throw RuntimeException("Error loading channel info: $message") },
                    { fn() })
        }
    }

    fun refreshChannelEntryInDb(context: Context, cid: String, errorFn: (String) -> Unit, successFn: (LoadChannelInfoResult) -> Unit) {
        val app = PotatoApplication.getInstance(context)
        val call = app.potatoApi.loadChannelInfo(app.apiKey, cid)
        callServiceBackground(call, {
            errorFn(it)
        }, {
            updateChannelInDatabase(context, it); successFn(it)
        })
    }

    private fun updateChannelInDatabase(context: Context, c: LoadChannelInfoResult) {
        val db = PotatoApplication.getInstance(context).cacheDatabase
        db.beginTransaction()
        try {
            db.delete(StorageHelper.CHANNELS_TABLE, "${StorageHelper.CHANNELS_ID} = ?", arrayOf(c.id))
            insertChannelIntoChannelsTable(db, c.id, c.domainId, c.name, c.unreadCount, c.privateUserId, false)
            db.setTransactionSuccessful()
        }
        finally {
            db.endTransaction()
        }
    }

    /**
     * Sets the NOTIFY_UNREAD value for all channels to false. This needs to be called
     * after the GCM registration has been reset (for example, after a full reset of
     * the device).
     */
    fun clearUnreadNotificationSettings(context: Context) {
        val db = PotatoApplication.getInstance(context).cacheDatabase
        val values = ContentValues()
        values.put(StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD, 0)
        db.update(StorageHelper.CHANNEL_CONFIG_TABLE, values, null, null)
    }

//    fun syncChannelDbToDataApi(context: Context) {
//        val db = PotatoApplication.getInstance(context).cacheDatabase
//        val result = db.query(StorageHelper.CHANNELS_TABLE, arrayOf(StorageHelper.CHANNELS_ID, StorageHelper.CHANNELS_DOMAIN, StorageHelper.CHANNELS_NAME),
//                null, null, null, null, null, null)
//        while (result.moveToNext()) {
//            val id = result.getString(0)
//            val domain = result.getString(1)
//            val name = result.getString(2)
//            Wearable.DataApi.putDataItem(apiClient)
//        }
//    }
}
