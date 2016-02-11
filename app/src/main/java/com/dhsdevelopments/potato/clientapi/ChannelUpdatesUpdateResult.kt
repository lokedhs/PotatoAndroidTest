package com.dhsdevelopments.potato.clientapi

import com.google.gson.annotations.SerializedName

class ChannelUpdatesUpdateResult {
    @SerializedName("result")
    lateinit var result: String

    override fun toString(): String{
        return "ChannelUpdatesUpdateResult(result='$result')"
    }
}
