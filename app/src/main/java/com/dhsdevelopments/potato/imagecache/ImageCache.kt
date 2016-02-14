package com.dhsdevelopments.potato.imagecache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.AsyncTask
import com.dhsdevelopments.potato.ImageHelpers
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.StorageHelper
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ImageCache(private val context: Context) {

    private val bitmapCache = HashMap<CacheKey, BitmapCacheEntry>()

    private var initialised = false
    private var shuttingDown = false
    private var shutDown = false

    private var cacheDir: File? = null
    private val db: SQLiteDatabase by lazy { PotatoApplication.getInstance(context).cacheDatabase }

    private var loadTaskIsActive = false
    private val loadQueue = LinkedList<LoadQueueEntry>()

    @Synchronized fun close() {
        synchronized (bitmapCache) {
            if (loadTaskIsActive) {
                shuttingDown = true
            }
            else {
                shutdownReal()
            }
        }
    }

    /**
     * This method is always to be called with the `bitmapCache` lock held.
     */
    private fun shutdownReal() {
        if (!shutDown) {
            shutDown = true
            shuttingDown = false
        }
    }

    fun loadImageFromApi(url: String, imageWidth: Int, imageHeight: Int, storageType: StorageType, callback: LoadImageCallback): Boolean {
        val app = PotatoApplication.getInstance(context)
        val apiKey = app.apiKey
        return loadImageInternal(url, imageWidth, imageHeight, storageType, callback, apiKey)
    }

    fun loadImage(url: String, imageWidth: Int, imageHeight: Int, storageType: StorageType, callback: LoadImageCallback): Boolean {
        return loadImageInternal(url, imageWidth, imageHeight, storageType, callback, null)
    }

    private fun loadImageInternal(url: String, imageWidth: Int, imageHeight: Int,
                                  storageType: StorageType, callback: LoadImageCallback,
                                  apiKey: String?): Boolean {
        initialiseIfNeeded()

        var shouldStartTask = false
        synchronized (bitmapCache) {
            if (shuttingDown) {
                return true
            }

            val cacheKey = CacheKey(url, imageWidth, imageHeight, apiKey != null)
            val cacheEntry = bitmapCache[cacheKey]
            if (cacheEntry == null) {
                val e = BitmapCacheEntry(true)
                bitmapCache.put(cacheKey, e)
                e.addCallback(callback)
                loadQueue.add(LoadQueueEntry(url, imageWidth, imageHeight, storageType, e, apiKey))
                if (!loadTaskIsActive) {
                    loadTaskIsActive = true
                    shouldStartTask = true
                }
                Log.d("created new cache entry. current load queue size=" + loadQueue.size + ", willStartNewTask=" + shouldStartTask)
            }
            else {
                if (cacheEntry.loading) {
                    cacheEntry.addCallback(callback)
                }
                else {
                    if (cacheEntry.bitmap != null) {
                        callback.bitmapLoaded(cacheEntry.bitmap!!)
                    }
                    else {
                        callback.bitmapNotFound()
                    }
                    return false
                }
            }
        }

        if (shouldStartTask) {
            val task = LoadImagesTask()
            task.execute()
        }

        return true
    }

    private fun addCacheEntryToDatabase(url: String, imageWidth: Int, imageHeight: Int, newFile: File?, storageType: StorageType, markFileAsAvailable: Boolean) {
        initialiseIfNeeded()

        val content = ContentValues()
        content.put(StorageHelper.IMAGE_CACHE_NAME, url)
        content.put(StorageHelper.IMAGE_CACHE_IMAGE_WIDTH, imageWidth)
        content.put(StorageHelper.IMAGE_CACHE_IMAGE_HEIGHT, imageHeight)
        content.put(StorageHelper.IMAGE_CACHE_FILENAME, newFile?.name)
        content.put(StorageHelper.IMAGE_CACHE_CREATED_DATE, System.currentTimeMillis())
        content.put(StorageHelper.IMAGE_CACHE_CAN_DELETE, storageType != StorageType.LONG)
        content.put(StorageHelper.IMAGE_CACHE_IMAGE_AVAILABLE, markFileAsAvailable)
        db.insert(StorageHelper.IMAGE_CACHE_TABLE, "filename", content)
    }

    private fun deleteCacheEntryFromDatabase(url: String, imageWidth: Int, imageHeight: Int) {
        initialiseIfNeeded()

        db.delete(StorageHelper.IMAGE_CACHE_TABLE,
                "${StorageHelper.IMAGE_CACHE_NAME} = ? and ${StorageHelper.IMAGE_CACHE_IMAGE_WIDTH} = ? and ${StorageHelper.IMAGE_CACHE_IMAGE_HEIGHT} = ?",
                arrayOf(url, imageWidth.toString(), imageHeight.toString()))
    }

    private fun findCachedFileInDatabase(db: SQLiteDatabase, cacheDirCopy: File, url: String, imageWidth: Int, imageHeight: Int): CachedFileResult? {
        db.beginTransaction()
        try {
            db.query(StorageHelper.IMAGE_CACHE_TABLE,
                    arrayOf("filename"),
                    "${StorageHelper.IMAGE_CACHE_NAME} = ? and ${StorageHelper.IMAGE_CACHE_IMAGE_WIDTH} = ? and ${StorageHelper.IMAGE_CACHE_IMAGE_HEIGHT} = ?",
                    arrayOf(url, imageWidth.toString(), imageHeight.toString()),
                    null,
                    null,
                    null).use { result ->
                if (!result.moveToNext()) {
                    db.setTransactionSuccessful()
                    return null
                }

                val filename = result.getString(0)

                var file: File? = null
                if (filename != null) {
                    file = File(cacheDirCopy, filename)
                    if (!file.exists()) {
                        deleteCacheEntryFromDatabase(url, imageWidth, imageHeight)
                        file = null
                    }
                }

                db.setTransactionSuccessful()

                return CachedFileResult(file)
            }
        }
        finally {
            db.endTransaction()
        }
    }

    private fun initialiseIfNeeded() {
        synchronized (bitmapCache) {
            if (initialised) {
                return
            }
            if (shutDown) {
                throw IllegalStateException("trying to initialise when already shut down")
            }

            val root = context.cacheDir
            if (root != null) {
                val dir = File(root, IMAGE_CACHE_DIR_NAME)
                if (!dir.exists()) {
                    if (dir.mkdir()) {
                        cacheDir = dir
                    }
                }
                else {
                    cacheDir = dir
                }
            }

            initialised = true
        }
    }

    class DeletableEntry(var name: String, var imageWidth: Int, var imageHeight: Int)

    fun purge(cutoffOffsetLong: Long, cutoffOffsetShort: Long) {
        initialiseIfNeeded()

        val now = System.currentTimeMillis()
        val cutoffLong = now - cutoffOffsetLong
        val cutoffShort = now - cutoffOffsetShort

        val toDelete = ArrayList<DeletableEntry>()

        db.query(StorageHelper.IMAGE_CACHE_TABLE,
                arrayOf(StorageHelper.IMAGE_CACHE_NAME, StorageHelper.IMAGE_CACHE_FILENAME, StorageHelper.IMAGE_CACHE_IMAGE_WIDTH, StorageHelper.IMAGE_CACHE_IMAGE_HEIGHT, StorageHelper.IMAGE_CACHE_CREATED_DATE, StorageHelper.IMAGE_CACHE_CAN_DELETE),
                null, null,
                null, null,
                null).use { result ->
            while (result.moveToNext()) {
                val name = result.getString(0)
                val fileName = result.getString(1)
                val imageWidth = result.getInt(2)
                val imageHeight = result.getInt(3)
                val createdDate = result.getLong(4)
                val canDelete = result.getInt(5) != 0

                if (canDelete && createdDate < cutoffShort || !canDelete && createdDate < cutoffLong) {
                    toDelete.add(DeletableEntry(name, imageWidth, imageHeight))
                    if (fileName != null) {
                        val file = File(cacheDir, fileName)
                        if (!file.delete()) {
                            Log.w("could not delete file: " + file)
                        }
                    }
                }
                else {
                    // If the file has been deleted from the cache, then we can remove if from the database immediately.
                    if (fileName != null) {
                        val file = File(cacheDir, fileName)
                        if (!file.exists()) {
                            toDelete.add(DeletableEntry(name, imageWidth, imageHeight))
                        }
                    }
                }
            }
        }

        for (e in toDelete) {
            db.delete(StorageHelper.IMAGE_CACHE_TABLE,
                    StorageHelper.IMAGE_CACHE_NAME + " = ? and " + StorageHelper.IMAGE_CACHE_IMAGE_WIDTH + " = ? and " + StorageHelper.IMAGE_CACHE_IMAGE_HEIGHT + " = ?",
                    arrayOf(e.name, e.imageWidth.toString(), e.imageHeight.toString()))
        }
    }

    private class BackgroundLoadResult(
            private val url: String,
            internal val bitmapCacheEntry: BitmapCacheEntry,
            internal val bitmap: Bitmap?) {
        override fun toString(): String {
            return "BackgroundLoadResult[url='$url', bitmapCacheEntry=$bitmapCacheEntry, bitmap=$bitmap]"
        }
    }

    @Suppress("IfNullToElvis")
    private inner class LoadImagesTask : AsyncTask<Void, BackgroundLoadResult, Void>() {
        override fun doInBackground(vararg voids: Void): Void? {
            while (true) {
                val queueEntry = nextEntryAndMaybeUpdateStatus()
                if (queueEntry == null) {
                    break
                }

                val cacheDirCopy = synchronized (this) { cacheDir }

                if (cacheDirCopy == null) {
                    // Session has been closed, cancel right away
                    return null
                }

                val result = findCachedFileInDatabase(db, cacheDirCopy, queueEntry.url, queueEntry.imageWidth, queueEntry.imageHeight)
                Log.d("cached image file=" + result + ", for url=" + queueEntry.url)
                val wasCached = result != null
                val cachedFile: File?
                if (!wasCached) {
                    try {
                        cachedFile = copyUrlToFile(cacheDirCopy, queueEntry.url, null, queueEntry.apiKey)
                        addCacheEntryToDatabase(queueEntry.url, queueEntry.imageWidth, queueEntry.imageHeight, cachedFile, queueEntry.storageType, false)
                    }
                    catch (e: IOException) {
                        Log.w("failed to load image: '" + queueEntry.url + "'", e)
                        cachedFile = null
                    }
                    catch (e: FileDownloadFailedException) {
                        Log.w("failed to load image: '" + queueEntry.url + "'", e)
                        cachedFile = null
                    }

                }
                else {
                    cachedFile = result!!.file
                }

                var bitmap: Bitmap? = null
                if (cachedFile != null) {
                    bitmap = ImageHelpers.loadAndScaleBitmap(cachedFile.path, queueEntry.imageWidth, queueEntry.imageHeight)
                    // If the file should not be stored in the cache, and it was loaded (i.e. it wasn't already
                    // stored in the cache) it should be deleted at this point.
                    if (bitmap == null || queueEntry.storageType == StorageType.DONT_STORE && !wasCached) {
                        removeOldFile(queueEntry.url, queueEntry.imageWidth, queueEntry.imageHeight, cachedFile)
                    }
                }

                publishProgress(BackgroundLoadResult(queueEntry.url,
                        queueEntry.bitmapCacheEntry,
                        bitmap))
            }

            synchronized (bitmapCache) {
                if (shuttingDown) {
                    shutdownReal()
                }
            }

            return null
        }

        private fun nextEntryAndMaybeUpdateStatus(): LoadQueueEntry? {
            synchronized (bitmapCache) {
                if (loadQueue.isEmpty()) {
                    loadTaskIsActive = false
                    return null
                }
                else {
                    return loadQueue.removeAt(0)
                }
            }
        }

        private fun removeOldFile(url: String, imageWidth: Int, imageHeight: Int, file: File) {
            if (!file.delete()) {
                Log.w("failed to delete file: " + file)
            }
            deleteCacheEntryFromDatabase(url, imageWidth, imageHeight)
        }

        override fun onProgressUpdate(vararg values: BackgroundLoadResult) {
            val result = values[0]
            Log.d("onProgressUpdate. result=" + result)
            val callbacksAndBitmap: Pair<List<LoadImageCallback>, Bitmap?> = synchronized (bitmapCache) {
                val entry = result.bitmapCacheEntry
                val bitmap = result.bitmap
                entry.bitmap = bitmap
                val callbacksCopy = ArrayList(entry.callbacks)
                entry.loading = false
                entry.callbacks.clear()
                Pair(callbacksCopy, bitmap)
            }

            Log.d("before calling callbacks. n=" + callbacksAndBitmap.first.size + ", bm=" + callbacksAndBitmap.second)
            for (callback in callbacksAndBitmap.first) {
                val b = callbacksAndBitmap.second
                if (b == null) {
                    callback.bitmapNotFound()
                }
                else {
                    callback.bitmapLoaded(b)
                }
            }
        }
    }

    companion object {
        private val IMAGE_CACHE_DIR_NAME = "images"

        @Throws(IOException::class, FileDownloadFailedException::class)
        fun copyUrlToFile(cacheDirCopy: File, url: String, tmpFilePrefix: String?, apiKey: String?): File? {
            val urlString = if (apiKey == null) url else PotatoApplication.API_URL_PREFIX + url

            val buf = StringBuilder()
            if (tmpFilePrefix != null) {
                buf.append(tmpFilePrefix)
            }
            for (i in 0..19) {
                buf.append(('a' + (Math.random() * ('z' - 'a' + 1)).toInt()).toChar())
            }
            buf.append('_')
            val s = buf.toString()
            var found: File? = null
            for (i in 0..29) {
                val name = s + i
                val f = File(cacheDirCopy, name)
                if (f.createNewFile()) {
                    found = f
                    break
                }
            }

            if (found == null) {
                Log.w("failed to create file name")
                return null
            }

            val client = OkHttpClient()
            val builder = Request.Builder()
            builder.url(urlString)
            if (apiKey != null) {
                builder.addHeader("API-token", apiKey)
            }
            val req = builder.build()
            val call = client.newCall(req)
            Log.d("Downloading url: $urlString")
            val response = call.execute()
            Log.d("After download attempt, isSuccessful=${response.isSuccessful}, code=${response.code()}")
            if (!response.isSuccessful) {
                if (response.code() == 404) {
                    // Simply return null here since we want to cache the 404's
                    if (!found.delete()) {
                        Log.w("Failed to delete unused temp file: " + found)
                    }
                    return null
                }
                else {
                    Log.w("Unable to load url: " + response.message())
                    throw FileDownloadFailedException("Got error response from server. code=" + response.code() + ", message=" + response.message())
                }
            }

            try {
                response.body().byteStream().use { inStream ->
                    FileOutputStream(found).use { outStream ->
                        val fileBuf = ByteArray(1024 * 16)
                        while (true) {
                            val n = inStream.read(fileBuf)
                            if (n == -1) {
                                break
                            }
                            outStream.write(fileBuf, 0, n)
                        }
                    }
                }
            }
            catch (e: IOException) {
                if (!found.delete()) {
                    Log.w("error when trying to delete broken download file: " + found)
                }
                throw e
            }

            return found
        }
    }
}
