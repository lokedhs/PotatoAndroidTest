package com.dhsdevelopments.potato.imagecache

import android.graphics.Bitmap
import java.util.*

internal class BitmapCacheEntry(var loading: Boolean) {
    var bitmap: Bitmap? = null
    var callbacks: MutableList<LoadImageCallback> = ArrayList()

    fun addCallback(callback: LoadImageCallback) {
        callbacks.add(callback)
    }
}
