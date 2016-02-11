package com.dhsdevelopments.potato.clientapi.channel2

import com.google.gson.annotations.SerializedName

class Domain {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("domain-type")
    lateinit var type: String

    @SerializedName("channels")
    lateinit var channels: List<Channel>

    override fun toString(): String {
        return "Domain[id='$id', name='$name', type='$type', channels=$channels]"
    }
}
