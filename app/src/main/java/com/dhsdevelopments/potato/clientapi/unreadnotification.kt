package com.dhsdevelopments.potato.clientapi.unreadnotification

import com.google.gson.annotations.SerializedName

@Suppress("unused")
class UpdateUnreadNotificationRequest(
        @SerializedName("token")
        var token: String,

        @SerializedName("add")
        var add: Boolean)

class UpdateUnreadNotificationResult {
    @SerializedName("result")
    lateinit var result: String
}
