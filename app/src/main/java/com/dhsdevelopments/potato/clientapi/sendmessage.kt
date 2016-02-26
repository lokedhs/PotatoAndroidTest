package com.dhsdevelopments.potato.clientapi.sendmessage

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

class SendMessageRequest {
    @SerializedName("text")
    lateinit var text: String

    constructor() {
    }

    constructor(text: String) {
        this.text = text
    }
}

class SendMessageResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("id")
    var id: String? = null

    override fun errorMsg(): String? {
        return if(result == "ok") null else result
    }
}
