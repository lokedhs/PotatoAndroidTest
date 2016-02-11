package com.dhsdevelopments.potato.clientapi.notifications

import com.dhsdevelopments.potato.clientapi.message.Message
import com.google.gson.annotations.SerializedName

class MessageNotification : PotatoNotification() {
    @SerializedName("c")
    lateinit var message: Message
}
