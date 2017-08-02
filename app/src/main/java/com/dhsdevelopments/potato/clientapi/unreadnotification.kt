package com.dhsdevelopments.potato.clientapi.unreadnotification

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

@Suppress("unused")
class UpdateUnreadNotificationRequest(
        @SerializedName("token")
        var token: String,

        @SerializedName("add")
        var add: Boolean,

        @SerializedName("provider")
        var provider: String)

class UpdateUnreadNotificationResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    override fun errorMsg(): String? {
        return if (result == "ok") null else result
    }
}
