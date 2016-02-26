package com.dhsdevelopments.potato.clientapi.deletemessage

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

@Suppress("unused")
class DeleteMessageResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("id")
    var messageId: String? = null

    override fun errorMsg(): String? {
        return if (result == "ok") null else result
    }
}
