package com.dhsdevelopments.potato.clientapi.deletemessage

import com.google.gson.annotations.SerializedName

class DeleteMessageResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("id")
    var messageId: String? = null
}
