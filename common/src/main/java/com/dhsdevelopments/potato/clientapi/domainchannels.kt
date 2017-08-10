@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.domainchannels

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

class Channel {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("private")
    var private: Boolean = false
}

class Group {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("type")
    lateinit var type: String

    @SerializedName("channels")
    var channels: List<Channel>? = null
}

class DomainInfoResult : RemoteResult {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("type")
    lateinit var type: String

    @SerializedName("groups")
    var groups: List<Group>? = null

    override fun errorMsg(): String? {
        return null
    }
}
