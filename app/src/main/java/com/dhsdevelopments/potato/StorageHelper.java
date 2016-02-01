package com.dhsdevelopments.potato;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StorageHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 3;

    public static final String IMAGE_CACHE_TABLE = "imagecache";
    public static final String IMAGE_CACHE_NAME = "name";
    public static final String IMAGE_CACHE_FILENAME = "filename";
    public static final String IMAGE_CACHE_CREATED_DATE = "createdDate";
    public static final String IMAGE_CACHE_IMAGE_AVAILABLE = "imageAvailable";
    public static final String IMAGE_CACHE_CAN_DELETE = "canDelete";

    public static final String DOMAINS_TABLE = "domains";
    public static final String DOMAINS_ID = "id";
    public static final String DOMAINS_NAME = "name";

    public static final String CHANNELS_TABLE = "channels";
    public static final String CHANNELS_ID = "id";
    public static final String CHANNELS_DOMAIN = "domain";
    public static final String CHANNELS_NAME = "name";
    public static final String CHANNELS_UNREAD = "unread";
    public static final String CHANNELS_PRIVATE = "private_user";

    public static final String CHANNEL_CONFIG_TABLE = "channel_config";
    public static final String CHANNEL_CONFIG_ID = "id";
    public static final String CHANNEL_CONFIG_SHOW_NOTIFICATIONS = "show_notification";
    public static final String CHANNEL_CONFIG_NOTIFY_UNREAD = "show_unread";

    public StorageHelper( Context context ) {
        super( context, "potatoData", null, DATABASE_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        db.execSQL( "create table " + IMAGE_CACHE_TABLE + " (" +
                            IMAGE_CACHE_NAME + " text primary key, " +
                            IMAGE_CACHE_FILENAME + " text, " +
                            IMAGE_CACHE_CREATED_DATE + " int not null, " +
                            IMAGE_CACHE_IMAGE_AVAILABLE + " boolean, " +
                            IMAGE_CACHE_CAN_DELETE + " boolean" +
                            ")" );
        createChannelTables( db );
        createChannelConfigTable( db );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        if( oldVersion < 2 ) {
            createChannelTables( db );
        }
        if( oldVersion < 3 ) {
            createChannelConfigTable( db );
        }
    }

    private void createChannelTables( SQLiteDatabase db ) {
        db.execSQL( "create table " + DOMAINS_TABLE + " (" +
                            DOMAINS_ID + " text primary key, " +
                            DOMAINS_NAME + " text not null" +
                            ")" );
        db.execSQL( "create table " + CHANNELS_TABLE + " (" +
                            CHANNELS_ID + " text primary key, " +
                            CHANNELS_DOMAIN + " text not null, " +
                            CHANNELS_NAME + " text not null, " +
                            CHANNELS_UNREAD + " text not null, " +
                            CHANNELS_PRIVATE + " text null, " +
                            "foreign key (domain) references " + DOMAINS_TABLE + "(" + DOMAINS_ID + ")" +
                            ")" );
    }

    private void createChannelConfigTable( SQLiteDatabase db ) {
        db.execSQL( "create table " + CHANNEL_CONFIG_TABLE + " (" +
                            CHANNEL_CONFIG_ID + " text primary key, " +
                            CHANNEL_CONFIG_SHOW_NOTIFICATIONS + " boolean not null, " +
                            CHANNEL_CONFIG_NOTIFY_UNREAD + " boolean not null" +
                            ")" );
    }
}
