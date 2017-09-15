package com.dhsdevelopments.potato.common

import android.arch.persistence.room.*
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dhsdevelopments.potato.clientapi.callServiceBackground
import com.dhsdevelopments.potato.clientapi.channelinfo.LoadChannelInfoResult
import java.util.*

@Entity(tableName = "channels",
        foreignKeys = arrayOf(ForeignKey(
                entity = DomainDescriptor::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("domain_id"))),
        indices = arrayOf(Index("domain_id")))
class ChannelDescriptor(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String = "",
        @ColumnInfo(name = "name")
        var name: String = "",
        @ColumnInfo(name = "private_user")
        var privateUser: String? = null,
        @ColumnInfo(name = "hidden")
        var hidden: Boolean = false,
        @ColumnInfo(name = "domain_id")
        var domainId: String = "",
        @ColumnInfo(name = "unread")
        var unreadCount: Int = 0)

@Dao
interface ChannelDao {
    @Query("select * from channels")
    fun findAll(): List<ChannelDescriptor>

    @Query("select * from channels where id = :arg0")
    fun findById(id: String): ChannelDescriptor?

    @Query("select id from channels where id = :arg0")
    fun findCollectionById(id: String): List<String>

    @Query("select * from channels where domain_id = :arg0")
    fun findByDomain(domainId: String): List<ChannelDescriptor>

    @Query("select * from channels where unread > 0")
    fun findUnread(): List<ChannelDescriptor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannel(channel: ChannelDescriptor)

    @Delete
    fun deleteChannel(channel: ChannelDescriptor)

    @Update
    fun updateChannel(channel: ChannelDescriptor)
}


@Entity(tableName = "domains")
class DomainDescriptor(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String = "",
        @ColumnInfo(name = "name")
        var name: String = "")

@Dao
interface DomainDao {
    @Query("select * from domains")
    fun findAll(): List<DomainDescriptor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDomain(domain: DomainDescriptor)
}

@Entity(tableName = "channel_config")
class ChannelConfigDescriptor(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var channelId: String = "",
        @ColumnInfo(name = "show_notification")
        var showNotification: Boolean = false,
        @ColumnInfo(name = "show_unread")
        var showUnread: Boolean = false
)

@Dao
interface ChannelConfigDao {
    @Query("select * from channel_config where id = :arg0")
    fun findByChannelId(id: String): ChannelConfigDescriptor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelConfig(channelConfig: ChannelConfigDescriptor)

    @Update
    fun updateChannelConfig(channelConfig: ChannelConfigDescriptor)
}

@Database(entities = arrayOf(ChannelDescriptor::class, DomainDescriptor::class, ChannelConfigDescriptor::class), version = 1, exportSchema = false)
abstract class PotatoDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun domainDao(): DomainDao
    abstract fun channelConfigDao(): ChannelConfigDao

    fun deleteChannelsAndDomains() {
        query("delete from channels", null).close()
        query("delete from domains", null).close()
    }

    fun updateShowUnread(cid: String, showUnread: Boolean) {
        val channelConfig = channelConfigDao().findByChannelId(cid)
        if (channelConfig == null) {
            channelConfigDao().insertChannelConfig(ChannelConfigDescriptor(cid, false, showUnread))
        }
        else if (channelConfig.showUnread != showUnread) {
            channelConfig.showUnread = showUnread
            channelConfigDao().updateChannelConfig(channelConfig)
        }
    }

    fun updateVisibility(cid: String, hidden: Boolean) {
        query("update channels set hidden = ? where id = ?", arrayOf(hidden, cid))
    }

    fun deleteChannel(cid: String) {
        query("delete from channel where id = ?", arrayOf(cid))
    }
}

object DbTools {

    fun makePotatoDb(context: Context): PotatoDatabase {
        val db = Room.databaseBuilder(context, PotatoDatabase::class.java, "potatoConfig")
                .allowMainThreadQueries()
                .build()
        if (db == null) {
            throw IllegalStateException("Error instantiating the database")
        }
        return db
    }

    fun loadChannelInfoFromDb(context: Context, cid: String): ChannelDescriptor {
        val db = CommonApplication.getInstance(context).cacheDatabase
        val channel = db.channelDao().findById(cid)
        if (channel == null) {
            throw IllegalArgumentException("Attempt to load nonexistent channel")
        }
        return channel
    }

    fun loadChannelConfigFromDb(context: Context, channelId: String): ChannelConfigDescriptor {
        val db = CommonApplication.getInstance(context).cacheDatabase
        return db.channelConfigDao().findByChannelId(channelId)!! // TODO: can this happen?
    }

    fun loadDomainsFromDb(context: Context): List<DomainDescriptor> {
        val db = CommonApplication.getInstance(context).cacheDatabase
        return db.domainDao().findAll()
    }

    fun loadAllChannelIdsInDomain(context: Context, domainId: String): Set<String> {
        val db = CommonApplication.getInstance(context).cacheDatabase
        return db.channelDao().findByDomain(domainId).map { it.id }.toSet()
    }

    fun isChannelJoined(context: Context, cid: String): Boolean {
        val db = CommonApplication.getInstance(context).cacheDatabase
        val res = db.channelDao().findCollectionById(cid)
        return res.isNotEmpty()
    }

    fun insertChannelIntoChannelsTable(db: PotatoDatabase, channelId: String, domainId: String, name: String, unreadCount: Int, privateUser: String?, hide: Boolean) {
        val channel = ChannelDescriptor(channelId, name, privateUser, hide, domainId, unreadCount)
        db.channelDao().insertChannel(channel)
    }

    fun ensureChannelInfo(context: Context, cid: String, fn: () -> Unit) {
        val db = CommonApplication.getInstance(context).cacheDatabase
        if (db.channelDao().findCollectionById(cid).isEmpty()) {
            fn()
        }
        else {
            refreshChannelEntryInDb(context, cid,
                    { message -> throw RuntimeException("Error loading channel info: $message") },
                    { fn() })
        }
    }

    fun refreshChannelEntryInDb(context: Context, cid: String, errorFn: (String) -> Unit, successFn: (LoadChannelInfoResult) -> Unit) {
        val app = CommonApplication.getInstance(context)
        val call = app.findApiProvider().makePotatoApi().loadChannelInfo(app.findApiKey(), cid)
        callServiceBackground(call, {
            errorFn(it)
        }, {
            updateChannelInDatabase(context, it); successFn(it)
        })
    }

    private fun updateChannelInDatabase(context: Context, c: LoadChannelInfoResult) {
        val db = CommonApplication.getInstance(context).cacheDatabase
        db.runInTransaction {
            db.query("delete from channels where id = ?", arrayOf(c.id)).close()
            insertChannelIntoChannelsTable(db, c.id, c.domainId, c.name, c.unreadCount, c.privateUserId, false)
        }
    }

    /**
     * Sets the NOTIFY_UNREAD value for all channels to false. This needs to be called
     * after the GCM registration has been reset (for example, after a full reset of
     * the device).
     */
    fun clearUnreadNotificationSettings(context: Context) {
        val db = CommonApplication.getInstance(context).cacheDatabase
        db.query("update channels set unread = 0", null)
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
