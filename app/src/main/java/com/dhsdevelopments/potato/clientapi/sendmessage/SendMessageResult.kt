package com.dhsdevelopments.potato.clientapi.sendmessage

import com.google.gson.annotations.SerializedName

class SendMessageResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("id")
    var id: String? = null
}
