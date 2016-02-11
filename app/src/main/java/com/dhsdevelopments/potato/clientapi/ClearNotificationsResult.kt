package com.dhsdevelopments.potato.clientapi

import com.google.gson.annotations.SerializedName

class ClearNotificationsResult {
    @SerializedName("result")
    lateinit var result: String

    override fun toString(): String {
        return "ClearNotificationsResult[result='$result']"
    }
}
