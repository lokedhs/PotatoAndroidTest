@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.editchannel

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

@Suppress("unused")
class LeaveChannelResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("detail")
    lateinit var detail: String

    override fun errorMsg() = if (result == "ok") null else result
}

class UpdateChannelVisibilityRequest() {
    constructor(show: Boolean) : this() {
        this.show = show
    }

    @SerializedName("show")
    var show: Boolean = false
}

class UpdateChannelVisibilityResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    override fun errorMsg() = if (result == "ok") null else result
}
