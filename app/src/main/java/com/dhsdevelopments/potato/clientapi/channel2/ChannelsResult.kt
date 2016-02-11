package com.dhsdevelopments.potato.clientapi.channel2

import com.google.gson.annotations.SerializedName

class ChannelsResult {
    @SerializedName("domains")
    lateinit var domains: List<Domain>

    override fun toString(): String {
        return "ChannelsResult[domains=$domains]"
    }
}
