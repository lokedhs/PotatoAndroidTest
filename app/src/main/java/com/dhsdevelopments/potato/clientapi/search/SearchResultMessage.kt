package com.dhsdevelopments.potato.clientapi.search

import com.google.gson.annotations.SerializedName

class SearchResultMessage {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("sender_id")
    lateinit var senderId: String

    @SerializedName("sender_name")
    lateinit var senderName: String

    @SerializedName("created_date")
    lateinit var createdDate: String

    @SerializedName("content")
    lateinit var content: String
}
