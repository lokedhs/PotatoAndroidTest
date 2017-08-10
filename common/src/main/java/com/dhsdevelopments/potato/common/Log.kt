package com.dhsdevelopments.potato.common

object Log {
    val LOG_TAG = "potato.common"

    fun e(message: String) {
        android.util.Log.e(LOG_TAG, message)
    }

    fun e(message: String, e: Throwable) {
        android.util.Log.e(LOG_TAG, message, e)
    }

    fun w(message: String) {
        android.util.Log.w(LOG_TAG, message)
    }

    fun w(message: String, e: Throwable) {
        android.util.Log.w(LOG_TAG, message, e)
    }

    fun i(message: String) {
        android.util.Log.i(LOG_TAG, message)
    }

    fun i(message: String, e: Throwable) {
        android.util.Log.i(LOG_TAG, message, e)
    }

    fun d(message: String) {
        android.util.Log.d(LOG_TAG, message)
    }

    fun d(message: String, e: Throwable) {
        android.util.Log.d(LOG_TAG, message, e)
    }

    fun wtf(message: String) {
        android.util.Log.e(LOG_TAG, message)
        throw RuntimeException(message)
    }

    fun wtf(message: String, e: Throwable) {
        android.util.Log.e(LOG_TAG, message, e)
        throw RuntimeException(message, e)
    }
}
