package com.dhsdevelopments.potato.clientapi.sendmessage

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

class SendMessageResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("id")
    var id: String? = null
}
