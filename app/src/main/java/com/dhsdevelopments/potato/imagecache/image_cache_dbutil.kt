package com.dhsdevelopments.potato.imagecache

import android.arch.persistence.room.*
import android.content.Context
import java.util.ArrayList

@Entity(tableName = "image_cache")
class ImageCacheEntry(
        @PrimaryKey
        @ColumnInfo(name = "name")
        var name: String,
        @ColumnInfo(name = "filename")
        var filename: String? = null,
        @ColumnInfo(name = "created_date")
        var createdDate: Long = 0,
        @ColumnInfo(name = "available")
        var imageAvailable: Boolean = false,
        @ColumnInfo(name = "can_delete")
        var canDelete: Boolean = false)

@Dao
interface ImageCacheDao {
    @Query("select * from image_cache")
    fun findAll(): List<ImageCacheEntry>

    @Query("select * from image_cache where name = :name")
    fun findByName(name: String): ImageCacheEntry?

    @Insert
    fun insertCacheEntry(cacheEntry: ImageCacheEntry)

    @Delete
    fun deleteCacheEntry(res: ImageCacheEntry)

    @Delete
    fun deleteCacheEntries(entryList: List<ImageCacheEntry>)
}

@Database(entities = arrayOf(ImageCacheEntry::class), version = 1, exportSchema = false)
abstract class ImageCacheDatabase : RoomDatabase() {
    abstract fun imageCacheDao(): ImageCacheDao

    fun deleteCacheEntry(url: String) {
        query("delete from image_cache where name = ?", arrayOf(url)).close()
    }
}

fun makeImagesCacheDb(context: Context): ImageCacheDatabase {
    return Room.databaseBuilder(context, ImageCacheDatabase::class.java, "image_cache")
            .allowMainThreadQueries()
            .build()
}
