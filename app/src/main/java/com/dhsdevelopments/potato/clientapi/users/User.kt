package com.dhsdevelopments.potato.clientapi.users

import com.google.gson.annotations.SerializedName

class User {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("description")
    lateinit var description: String

    @SerializedName("nickname")
    lateinit var nickname: String

    @SerializedName("image_name")
    lateinit var imageName: String

    override fun toString(): String {
        return "User[id='$id', description='$description', nickname='$nickname', imageName='$imageName']"
    }
}
