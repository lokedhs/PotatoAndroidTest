package com.dhsdevelopments.potato.clientapi

import com.google.gson.annotations.SerializedName

class ServerInfoResult {
    @SerializedName("version")
    lateinit var version: String

    @SerializedName("gcm_sender")
    var gcmSender: String? = null

    override fun toString() = "ServerInfo[version=$version, gcmSender=$gcmSender]"
}
