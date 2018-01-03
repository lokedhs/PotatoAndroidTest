@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.channel2

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

class Channel {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("hide")
    var hide: Boolean = false

    @SerializedName("group")
    lateinit var groupId: String

    @SerializedName("group_type")
    lateinit var groupType: String

    @SerializedName("unread_count")
    var unreadCount: Int = 0

    @SerializedName("private_user")
    var privateUser: String? = null

    override fun toString(): String {
        return "Channel[id='$id', name='$name', hide=$hide, groupId='$groupId', groupType='$groupType', unreadCount=$unreadCount, privateUser='$privateUser']"
    }
}

class ChannelsResult {
    @SerializedName("domains")
    lateinit var domains: List<Domain>

    override fun toString(): String {
        return "ChannelsResult[domains=$domains]"
    }
}

class Domain {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("type")
    lateinit var type: String

    @SerializedName("channels")
    lateinit var channels: List<Channel>

    override fun toString(): String {
        return "Domain[id='$id', name='$name', type='$type', channels=$channels]"
    }
}

class FindPrivateChannelIdResult : RemoteResult {
    @SerializedName("channel")
    lateinit var channel: String

    override fun errorMsg(): String? {
        return null
    }
}
