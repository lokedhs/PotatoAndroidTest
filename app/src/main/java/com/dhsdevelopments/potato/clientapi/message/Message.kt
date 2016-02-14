package com.dhsdevelopments.potato.clientapi.message

import com.google.gson.annotations.SerializedName

import java.io.Serializable

class Message : Serializable {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("channel")
    lateinit var channel: String

    @SerializedName("created_date")
    lateinit var createdDate: String

    @SerializedName("from")
    lateinit var from: String

    @SerializedName("from_name")
    lateinit var fromName: String

    @SerializedName("text")
    lateinit var text: MessageElement

    @SerializedName("use_math")
    var useMath: Boolean = false

    @SerializedName("deleted")
    var deleted: Boolean = false

    @SerializedName("hash")
    lateinit var hash: String

    @SerializedName("updated")
    var updated: Int? = null

    @SerializedName("updated_date")
    var updatedDate: String? = null

    @SerializedName("extra_html")
    var extraHtml: String? = null

    @SerializedName("star_users")
    lateinit var starUsers: List<String>

    @SerializedName("image")
    var messageImage: MessageImage? = null

    override fun toString(): String {
        return "Message[id='$id', channel='$channel', createdDate='$createdDate', from='$from', fromName='$fromName', text=$text, useMath=$useMath, deleted=$deleted, hash='$hash', updated=$updated, extraHtml='$extraHtml', starUsers=$starUsers]"
    }
}
