package com.dhsdevelopments.potato.clientapi.users

import com.google.gson.annotations.SerializedName

class LoadUsersResult {
    @SerializedName("members")
    lateinit var members: List<User>

    override fun toString(): String {
        return "LoadUsersResult[members=$members]"
    }
}
