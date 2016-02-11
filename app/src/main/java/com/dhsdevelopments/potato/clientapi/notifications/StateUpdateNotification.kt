package com.dhsdevelopments.potato.clientapi.notifications

import com.google.gson.annotations.SerializedName

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
