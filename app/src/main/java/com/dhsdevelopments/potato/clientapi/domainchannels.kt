package com.dhsdevelopments.potato.clientapi.domainchannels

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

class Channel {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("private")
    var isPrivate = false
}

class Group {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("channels")
    lateinit var channels: List<Channel>
}

class ChannelsInDomainResult : RemoteResult {
    @SerializedName("groups")
    lateinit var groups: List<Group>

    override fun errorMsg(): String? {
        return null
    }
}
