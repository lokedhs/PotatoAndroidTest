package com.dhsdevelopments.potato

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StorageHelper(context: Context) : SQLiteOpenHelper(context, "potatoData", null, StorageHelper.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createImageCacheTables(db)
        createChannelTables(db)
        createChannelConfigTable(db)
    }

    private fun createImageCacheTables(db: SQLiteDatabase) {
        db.execSQL("create table $IMAGE_CACHE_TABLE ($IMAGE_CACHE_NAME text not null, $IMAGE_CACHE_IMAGE_WIDTH int not null, $IMAGE_CACHE_IMAGE_HEIGHT int not null, $IMAGE_CACHE_FILENAME text, $IMAGE_CACHE_CREATED_DATE int not null, $IMAGE_CACHE_IMAGE_AVAILABLE boolean, $IMAGE_CACHE_CAN_DELETE boolean, primary key ($IMAGE_CACHE_NAME, $IMAGE_CACHE_IMAGE_WIDTH, $IMAGE_CACHE_IMAGE_HEIGHT))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    private fun createChannelTables(db: SQLiteDatabase) {
        db.execSQL("create table $DOMAINS_TABLE ($DOMAINS_ID text primary key, $DOMAINS_NAME text not null)")
        db.execSQL("create table $CHANNELS_TABLE ($CHANNELS_ID text primary key, $CHANNELS_DOMAIN text not null, $CHANNELS_NAME text not null, $CHANNELS_UNREAD text not null, $CHANNELS_PRIVATE text null, foreign key (domain) references $DOMAINS_TABLE($DOMAINS_ID))")
    }

    private fun createChannelConfigTable(db: SQLiteDatabase) {
        db.execSQL("create table $CHANNEL_CONFIG_TABLE ($CHANNEL_CONFIG_ID text primary key, $CHANNEL_CONFIG_SHOW_NOTIFICATIONS boolean not null, $CHANNEL_CONFIG_NOTIFY_UNREAD boolean not null)")
    }

    companion object {
        private val DATABASE_VERSION = 3

        val IMAGE_CACHE_TABLE = "imagecache"
        val IMAGE_CACHE_NAME = "name"
        val IMAGE_CACHE_FILENAME = "filename"
        val IMAGE_CACHE_IMAGE_WIDTH = "width"
        val IMAGE_CACHE_IMAGE_HEIGHT = "height"
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

        @JvmField val CHANNEL_CONFIG_TABLE = "channel_config"
        @JvmField val CHANNEL_CONFIG_ID = "id"
        @JvmField val CHANNEL_CONFIG_SHOW_NOTIFICATIONS = "show_notification"
        @JvmField val CHANNEL_CONFIG_NOTIFY_UNREAD = "show_unread"
    }
}
