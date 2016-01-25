package com.dhsdevelopments.potato.imagecache;

class LoadQueueEntry
{
    String url;
    int imageWidth;
    int imageHeight;
    StorageType storageType;
    BitmapCacheEntry bitmapCacheEntry;
    String apiKey;

    LoadQueueEntry( String url, int imageWidth, int imageHeight, StorageType storageType, BitmapCacheEntry bitmapCacheEntry, String apiKey ) {
        this.url = url;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.storageType = storageType;
        this.bitmapCacheEntry = bitmapCacheEntry;
        this.apiKey = apiKey;
    }
}
