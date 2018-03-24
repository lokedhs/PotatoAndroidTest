package com.dhsdevelopments.potato.usercache

import android.app.IntentService
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.LocalBroadcastManager
import com.dhsdevelopments.potato.common.Log
import paperparcel.PaperParcel
import java.lang.UnsupportedOperationException

class UserCacheService : IntentService("UserCache") {

    companion object {
        val REQUEST_USER_INFO = "${UserCacheService::class.qualifiedName}.requestUserInfo"
        val EXTRA_USER_ID = "${UserCacheService::class.qualifiedName}.userId"
        val BROADCAST_USER_INFO_UPDATED = "${UserCacheService::class.qualifiedName}.userInfoUpdated"
        val EXTRA_USER_INFO = "${UserCacheService::class.qualifiedName}.userInfo"
    }

    private val cache: Map<String,UserInfo> = HashMap()

    override fun onHandleIntent(intent: Intent) {
        when (intent.action) {
            REQUEST_USER_INFO -> requestUserInfo(intent)
            else -> throw UnsupportedOperationException("Action type '${intent.action}' not implemented")
        }
    }

    private fun requestUserInfo(intent: Intent) {
        val uid = intent.getStringExtra(EXTRA_USER_ID)!!
        Log.d("Got request for user info, user=$uid")

        val userInfo = cache[uid]
        if(userInfo != null) {
            sendUserInfoUpdate(userInfo)
        }
        else {

        }
    }

    private fun sendUserInfoUpdate(userInfo: UserInfo) {
        val intent = Intent(BROADCAST_USER_INFO_UPDATED).apply {
            putExtra(EXTRA_USER_INFO, userInfo)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}

@PaperParcel
data class UserInfo (
        val id: String,
        val nickname: String
) : Parcelable {

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = PaperParcelUserInfo.CREATOR
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelUserInfo.writeToParcel(this, dest, flags)
    }

    override fun describeContents(): Int = 0
}
