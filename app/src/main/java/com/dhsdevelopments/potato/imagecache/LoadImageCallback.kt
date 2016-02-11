package com.dhsdevelopments.potato.imagecache

import android.graphics.Bitmap

interface LoadImageCallback {
    fun bitmapLoaded(bitmap: Bitmap)

    fun bitmapNotFound()
}
