package com.dhsdevelopments.potato.clientapi.command

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

class SendCommandRequest {
    @SerializedName("channel")
    lateinit var channel: String

    @SerializedName("session_id")
    lateinit var session: String

    @SerializedName("command")
    lateinit var command: String

    @SerializedName("arg")
    lateinit var arg: String

    constructor() {
    }

    constructor(channel: String, session: String, command: String, arg: String) {
        this.channel = channel
        this.session = session
        this.command = command
        this.arg = arg
    }
}

class SendCommandResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    override fun errorMsg(): String? {
        val res = result;
        return if(res == "ok") null else res
    }
}
