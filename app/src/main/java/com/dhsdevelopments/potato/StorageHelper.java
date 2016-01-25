package com.dhsdevelopments.potato;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StorageHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;

    public static final String IMAGE_CACHE_TABLE = "imagecache";
    public static final String IMAGE_CACHE_NAME = "name";
    public static final String IMAGE_CACHE_FILENAME = "filename";
    public static final String IMAGE_CACHE_CREATED_DATE = "createdDate";
    public static final String IMAGE_CACHE_IMAGE_AVAILABLE = "imageAvailable";
    public static final String IMAGE_CACHE_CAN_DELETE = "canDelete";

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
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {

    }
}
