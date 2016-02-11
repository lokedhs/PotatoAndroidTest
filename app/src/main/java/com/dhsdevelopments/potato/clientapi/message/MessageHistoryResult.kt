package com.dhsdevelopments.potato.clientapi.message

import com.google.gson.annotations.SerializedName

class MessageHistoryResult {
    @SerializedName("messages")
    lateinit var messages: List<Message>

    override fun toString(): String {
        return "MessageHistoryResult[messages=$messages]"
    }
}
