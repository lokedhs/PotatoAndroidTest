package com.dhsdevelopments.potato.imagecache

internal class LoadQueueEntry(
        var url: String,
        var imageWidth: Int,
        var imageHeight: Int,
        var storageType: StorageType,
        var bitmapCacheEntry: BitmapCacheEntry,
        var apiKey: String?)
