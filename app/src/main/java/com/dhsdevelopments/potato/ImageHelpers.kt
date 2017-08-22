package com.dhsdevelopments.potato

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import com.dhsdevelopments.potato.common.Log

object ImageHelpers {
    private val LOG_2 = Math.log(2.0)

    fun loadAndScaleBitmap(file: String, width: Int, height: Int): Bitmap? {
        return loadAndScaleBitmapWithMinimumSize(file, width, height, -1, -1)
    }

    fun loadAndScaleBitmapWithMinimumSize(file: String, width: Int, height: Int, minimumWidth: Int, minimumHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file, options)

        if (minimumWidth != -1 && minimumHeight != -1 && (options.outWidth < minimumWidth || options.outHeight < minimumHeight)) {
            Log.d("skipping: w=${options.outWidth}, h=${options.outHeight}")
            return null
        }

        val ratio = Math.max(options.outWidth / width.toDouble(), options.outHeight / height.toDouble())
        val exp = Math.max(Math.floor(Math.log(ratio) / LOG_2).toInt(), 0)
        val scale = 1 shl exp
        Log.d("orig:(${options.outWidth},${options.outHeight}), req:($width,$height), scale=$scale")

        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        val tmpBitmap = BitmapFactory.decodeFile(file, o2) ?: return null

        val w = tmpBitmap.width
        val h = tmpBitmap.height

        val matrix = Matrix()
        val srcRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        val destRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        matrix.setRectToRect(srcRect, destRect, Matrix.ScaleToFit.CENTER)
        return Bitmap.createBitmap(tmpBitmap, 0, 0, w, h, matrix, true)
    }
}
