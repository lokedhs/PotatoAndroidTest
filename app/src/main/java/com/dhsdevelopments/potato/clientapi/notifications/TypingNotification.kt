package com.dhsdevelopments.potato.clientapi.notifications

import com.google.gson.annotations.SerializedName

class TypingNotification : PotatoNotification() {
    @SerializedName("user")
    lateinit var userId: String

    @SerializedName("channel")
    lateinit var channelId: String

    @SerializedName("add-type")
    lateinit var addType: String

    override fun toString(): String {
        return "TypingNotification[userId='$userId', channelId='$channelId', addType='$addType']"
    }
}
