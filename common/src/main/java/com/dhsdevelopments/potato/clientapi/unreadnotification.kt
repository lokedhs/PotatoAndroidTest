package com.dhsdevelopments.potato.clientapi

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

    override fun errorMsg() = if (result == "ok") null else result
}
