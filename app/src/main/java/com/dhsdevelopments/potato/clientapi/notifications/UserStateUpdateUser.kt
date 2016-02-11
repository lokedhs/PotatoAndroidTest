package com.dhsdevelopments.potato.clientapi.notifications

import com.google.gson.annotations.SerializedName

class UserStateUpdateUser {
    @SerializedName("id")
    lateinit var id: String

    override fun toString(): String {
        return "UserStateUpdateUser[id='$id']"
    }
}
