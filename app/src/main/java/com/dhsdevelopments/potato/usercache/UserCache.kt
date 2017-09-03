package com.dhsdevelopments.potato.usercache

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

class UserCache(val context: Context) {
    private val users: Map<String,UserInfo> = HashMap()

    fun getUserInfo(userId: String, callback: (UserInfo) -> Unit) {
        val userInfo = users.get(userId)
        if(userInfo != null) {
            callback(userInfo)
        }
    }
}
