package com.dhsdevelopments.potato.clientapi.notifications

import com.dhsdevelopments.potato.clientapi.message.Message
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MessageNotification : PotatoNotification() {
    @SerializedName("c")
    lateinit var message: Message
}

class StateUpdateNotification : PotatoNotification() {
    @SerializedName("add-type")
    lateinit var addType: String

    @SerializedName("user")
    var userStateUser: String? = null

    @SerializedName("users")
    var userStateSyncMembers: List<UserStateUpdateUser>? = null

    @SerializedName("channel")
    lateinit var channel: String

    override fun toString(): String {
        return "StateUpdateNotification[" +
                "addType='" + addType + '\'' +
                ", userStateUser='" + userStateUser + '\'' +
                ", userStateSyncMembers=" + userStateSyncMembers +
                ", channel='" + channel + '\'' +
                "] " + super.toString()
    }
}

class UserStateUpdateUser {
    @SerializedName("id")
    lateinit var id: String

    override fun toString(): String {
        return "UserStateUpdateUser[id='$id']"
    }
}

class PotatoNotificationResult {
    @SerializedName("event")
    lateinit var eventId: String

    @SerializedName("data")
    var notifications: List<PotatoNotification>? = null
}

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

open class PotatoNotification : Serializable {
    @SerializedName("type")
    lateinit var type: String


    override fun toString(): String {
        return "PotatoNotification[type='$type']"
    }
}
