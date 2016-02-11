package com.dhsdevelopments.potato.clientapi.notifications

import com.google.gson.annotations.SerializedName

class PotatoNotificationResult {
    @SerializedName("event")
    lateinit var eventId: String

    @SerializedName("data")
    var notifications: List<PotatoNotification>? = null
}
