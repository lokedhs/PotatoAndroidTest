package com.dhsdevelopments.potato.imagecache

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.AsyncTask
import com.dhsdevelopments.potato.ImageHelpers
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.common.StorageHelper
import com.dhsdevelopments.potato.common.makeRandomFile
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.SoftReference
import java.util.*

internal data class CacheKey(
        private val url: String,
        private val width: Int,
        private val height: Int,
        private val fromApi: Boolean)

class ImageCache(private val context: Context) {

    private val bitmapCache = HashMap<CacheKey, BitmapCacheEntry>()

    private var shuttingDown = false
    private var shutDown = false

    private val db: SQLiteDatabase by lazy { PotatoApplication.getInstance(context).cacheDatabase }

    private var loadTaskIsActive = false
    private val loadQueue = LinkedList<LoadQueueEntry>()

    private val cacheDir: File? by lazy {
        val root = context.cacheDir

        if (root == null) {
            Log.e("No cache directory found")
            null
        }
        else {
            val dir = File(root, IMAGE_CACHE_DIR_NAME)
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    Log.e("Unable to create cache directory")
                    null
                }
                else {
                    dir
                }
            }
            else {
                dir
            }
        }
    }

    @Synchronized fun close() {
        synchronized(bitmapCache) {
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
        val apiKey = app.findApiKey()
        return loadImageInternal(app.findApiProvider().apiUrlPrefix + (if (url.startsWith("/")) url.substring(1) else url), imageWidth, imageHeight, storageType, callback, apiKey)
    }

    fun loadImage(url: String, imageWidth: Int, imageHeight: Int, storageType: StorageType, callback: LoadImageCallback): Boolean {
        return loadImageInternal(url, imageWidth, imageHeight, storageType, callback, null)
    }

    private fun loadImageInternal(url: String, imageWidth: Int, imageHeight: Int,
                                  storageType: StorageType, callback: LoadImageCallback,
                                  apiKey: String?): Boolean {
        var shouldStartTask = false
        synchronized(bitmapCache) {
            if (shuttingDown || shutDown) {
                return true
            }

            val cacheKey = CacheKey(url, imageWidth, imageHeight, apiKey != null)
            var cacheEntry = bitmapCache[cacheKey]
            var cachedBitmap: Bitmap? = null
            if (cacheEntry != null) {
                val ref = cacheEntry.bitmap
                if (ref != null) {
                    val bm = ref.get()
                    if (bm == null) {
                        bitmapCache.remove(cacheKey)
                        cacheEntry = null
                    }
                    else {
                        cachedBitmap = bm
                    }
                }
            }
            if (cacheEntry == null) {
                val e = BitmapCacheEntry(true)
                bitmapCache.put(cacheKey, e)
                e.addCallback(callback)
                loadQueue.add(LoadQueueEntry(url, imageWidth, imageHeight, storageType, e, apiKey))
                if (!loadTaskIsActive) {
                    loadTaskIsActive = true
                    shouldStartTask = true
                }
                Log.d("created new cache entry. current load queue size=${loadQueue.size}, willStartNewTask=${shouldStartTask}")
                Unit
            }
            else {
                if (cacheEntry.loading) {
                    cacheEntry.addCallback(callback)
                }
                else {
                    if (cacheEntry.bitmap != null) {
                        callback.bitmapLoaded(cachedBitmap!!)
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

    private fun addCacheEntryToDatabase(url: String, newFile: File?, storageType: StorageType, markFileAsAvailable: Boolean) {
        val content = ContentValues()
        content.put(StorageHelper.IMAGE_CACHE_NAME, url)
        content.put(StorageHelper.IMAGE_CACHE_FILENAME, newFile?.name)
        content.put(StorageHelper.IMAGE_CACHE_CREATED_DATE, System.currentTimeMillis())
        content.put(StorageHelper.IMAGE_CACHE_CAN_DELETE, storageType != StorageType.LONG)
        content.put(StorageHelper.IMAGE_CACHE_IMAGE_AVAILABLE, markFileAsAvailable)
        db.insert(StorageHelper.IMAGE_CACHE_TABLE, "filename", content)
    }

    private fun deleteCacheEntryFromDatabase(url: String) {
        db.delete(StorageHelper.IMAGE_CACHE_TABLE,
                "${StorageHelper.IMAGE_CACHE_NAME} = ?",
                arrayOf(url))
    }

    private fun findCachedFileInDatabase(db: SQLiteDatabase, url: String): CachedFileResult? {
        db.beginTransaction()
        try {
            db.query(StorageHelper.IMAGE_CACHE_TABLE,
                    arrayOf("filename"),
                    "${StorageHelper.IMAGE_CACHE_NAME} = ?",
                    arrayOf(url),
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
                    file = File(cacheDir, filename)
                    if (!file.exists()) {
                        deleteCacheEntryFromDatabase(url)
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

    fun purge(cutoffOffsetLong: Long, cutoffOffsetShort: Long) {
        if (cacheDir == null) {
            return
        }

        val now = System.currentTimeMillis()
        val cutoffLong = now - cutoffOffsetLong
        val cutoffShort = now - cutoffOffsetShort

        val toDelete = ArrayList<String>()

        db.query(StorageHelper.IMAGE_CACHE_TABLE,
                arrayOf(StorageHelper.IMAGE_CACHE_NAME, StorageHelper.IMAGE_CACHE_FILENAME, StorageHelper.IMAGE_CACHE_CREATED_DATE, StorageHelper.IMAGE_CACHE_CAN_DELETE),
                null, null,
                null, null,
                null).use { result ->
            while (result.moveToNext()) {
                val name = result.getString(0)
                val fileName = result.getString(1)
                val createdDate = result.getLong(2)
                val canDelete = result.getInt(3) != 0

                if (canDelete && createdDate < cutoffShort || !canDelete && createdDate < cutoffLong) {
                    toDelete.add(name)
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
                            toDelete.add(name)
                        }
                    }
                }
            }
        }

        for (name in toDelete) {
            db.delete(StorageHelper.IMAGE_CACHE_TABLE,
                    "${StorageHelper.IMAGE_CACHE_NAME} = ?",
                    arrayOf(name))
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

                val cacheDirCopy = cacheDir
                if (cacheDirCopy == null) {
                    return null
                }

                val result = findCachedFileInDatabase(db, queueEntry.url)
                Log.d("cached image file=$result, for url=${queueEntry.url}")
                val cachedFile =
                        if (result == null) {
                            try {
                                val file = copyUrlToFile(cacheDirCopy, queueEntry.url, "", queueEntry.apiKey)
                                addCacheEntryToDatabase(queueEntry.url, file, queueEntry.storageType, false)
                                file
                            }
                            catch (e: IOException) {
                                Log.w("failed to load image: '" + queueEntry.url + "'", e)
                                null
                            }
                            catch (e: FileDownloadFailedException) {
                                Log.w("failed to load image: '" + queueEntry.url + "'", e)
                                null
                            }

                        }
                        else {
                            result.file
                        }

                var bitmap: Bitmap? = null
                if (cachedFile != null) {
                    bitmap = ImageHelpers.loadAndScaleBitmap(cachedFile.path, queueEntry.imageWidth, queueEntry.imageHeight)
                    // If the file should not be stored in the cache, and it was loaded (i.e. it wasn't already
                    // stored in the cache) it should be deleted at this point.
                    if (bitmap == null || (queueEntry.storageType == StorageType.DONT_STORE && result == null)) {
                        removeOldFile(queueEntry.url, cachedFile)
                    }
                }

                publishProgress(BackgroundLoadResult(queueEntry.url,
                        queueEntry.bitmapCacheEntry,
                        bitmap))
            }

            synchronized(bitmapCache) {
                if (shuttingDown) {
                    shutdownReal()
                }
            }

            return null
        }

        private fun nextEntryAndMaybeUpdateStatus(): LoadQueueEntry? {
            synchronized(bitmapCache) {
                if (loadQueue.isEmpty()) {
                    loadTaskIsActive = false
                    return null
                }
                else {
                    return loadQueue.removeAt(0)
                }
            }
        }

        private fun removeOldFile(url: String, file: File) {
            if (!file.delete()) {
                Log.w("failed to delete file: " + file)
            }
            deleteCacheEntryFromDatabase(url)
        }

        override fun onProgressUpdate(vararg values: BackgroundLoadResult) {
            val result = values[0]
            val callbacksCopy: List<LoadImageCallback> = synchronized(bitmapCache) {
                val entry = result.bitmapCacheEntry
                entry.bitmap = if (result.bitmap != null) SoftReference(result.bitmap) else null
                entry.loading = false

                val callbacksCopy = ArrayList(entry.callbacks)
                entry.callbacks.clear()

                callbacksCopy
            }

            for (callback in callbacksCopy) {
                val b = result.bitmap
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
        fun copyUrlToFile(cacheDirCopy: File, url: String, tmpFilePrefix: String, apiKey: String?): File? {
            val found = makeRandomFile(cacheDirCopy, tmpFilePrefix)

            val client = OkHttpClient()
            val builder = Request.Builder()
            builder.url(url)
            if (apiKey != null) {
                builder.addHeader("API-token", apiKey)
            }
            val req = builder.build()
            val call = client.newCall(req)
            Log.d("Downloading url: $url with apiKey=$apiKey")
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
