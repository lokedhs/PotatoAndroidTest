package com.dhsdevelopments.potato.imagecache

internal class CacheKey(
        private val url: String,
        private val width: Int,
        private val height: Int,
        private val fromApi: Boolean) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val cacheKey = other as CacheKey

        if (width != cacheKey.width) {
            return false
        }
        if (height != cacheKey.height) {
            return false
        }
        if (fromApi != cacheKey.fromApi) {
            return false
        }
        return url == cacheKey.url

    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + if (fromApi) 1 else 0
        return result
    }
}
