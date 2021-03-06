package com.dhsdevelopments.potato.common

import android.arch.persistence.room.*
import android.content.Context
import com.dhsdevelopments.potato.clientapi.callServiceBackground
import com.dhsdevelopments.potato.clientapi.channelinfo.LoadChannelInfoResult

@Dao
interface ChannelDao {
    @Query("select * from channels")
    fun findAll(): List<ChannelDescriptor>

    @Query("select * from channels where id = :id")
    fun findById(id: String): ChannelDescriptor?

    @Query("select id from channels where id = :id")
    fun findCollectionById(id: String): List<String>

    @Query("select * from channels where domain_id = :domainId")
    fun findByDomain(domainId: String): List<ChannelDescriptor>

    @Query("select * from channels where unread > 0")
    fun findUnread(): List<ChannelDescriptor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannel(channel: ChannelDescriptor)

    @Delete
    fun deleteChannel(channel: ChannelDescriptor)

    @Update
    fun updateChannel(channel: ChannelDescriptor)

    @Query("delete from channels where id = :id")
    fun deleteById(id: String)

    @Query("update channels set hidden = :hidden where id = :id")
    fun updateChannelVisibility(id: String, hidden: Boolean)

    @Query("delete from channels")
    fun deleteAllChannels()
}

@Dao
interface DomainDao {
    @Query("select * from domains")
    fun findAll(): List<DomainDescriptor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDomain(domain: DomainDescriptor)

    @Query("delete from domains")
    fun deleteAllDomains()
}

@Dao
interface ChannelConfigDao {
    @Query("select * from channel_config where id = :id")
    fun findByChannelId(id: String): ChannelConfigDescriptor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelConfig(channelConfig: ChannelConfigDescriptor)

    @Update
    fun updateChannelConfig(channelConfig: ChannelConfigDescriptor)
}

@Database(entities = [ChannelDescriptor::class, DomainDescriptor::class, ChannelConfigDescriptor::class], version = 1, exportSchema = false)
abstract class PotatoDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun domainDao(): DomainDao
    abstract fun channelConfigDao(): ChannelConfigDao

    fun deleteChannelsAndDomains() {
        channelDao().deleteAllChannels()
        domainDao().deleteAllDomains()
    }

    fun updateShowUnread(cid: String, showUnread: Boolean) {
        val channelConfig = channelConfigDao().findByChannelId(cid)
        if (channelConfig == null) {
            channelConfigDao().insertChannelConfig(ChannelConfigDescriptor(cid, false, showUnread))
        } else if (channelConfig.showUnread != showUnread) {
            channelConfig.showUnread = showUnread
            channelConfigDao().updateChannelConfig(channelConfig)
        }
    }

    fun updateVisibility(cid: String, hidden: Boolean) {
        channelDao().updateChannelVisibility(cid, hidden)
    }

    fun deleteChannel(cid: String) {
        channelDao().deleteById(cid)
    }
}

object DbTools {

    fun makePotatoDb(context: Context): PotatoDatabase {
        return Room.databaseBuilder(context, PotatoDatabase::class.java, "potatoConfig")
                .allowMainThreadQueries()
                .build()
    }

    fun loadChannelInfoFromDb(context: Context, cid: String): ChannelDescriptor {
        val db = CommonApplication.getInstance(context).cacheDatabase
        val channel = db.channelDao().findById(cid)
                ?: throw IllegalArgumentException("Attempt to load nonexistent channel")
        return channel
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
        } else {
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
