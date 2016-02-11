package com.dhsdevelopments.potato.clientapi.message

import com.google.gson.annotations.SerializedName

import java.io.Serializable

class MessageImage : Serializable {
    @SerializedName("file")
    lateinit var file: String

    @SerializedName("width")
    var width: Int = 0

    @SerializedName("height")
    var height: Int = 0
}
