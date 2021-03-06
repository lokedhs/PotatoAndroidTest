@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.users

import com.google.gson.annotations.SerializedName

class LoadUsersResult {
    @SerializedName("members")
    lateinit var members: List<User>

    override fun toString() = "LoadUsersResult[members=$members]"
}

class LoadUserResult {
    @SerializedName("user")
    lateinit var user: User
}

class User {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("description")
    lateinit var description: String

    @SerializedName("nickname")
    lateinit var nickname: String

    @SerializedName("image_name")
    lateinit var imageName: String

    override fun toString() = "User[id='$id', description='$description', nickname='$nickname', imageName='$imageName']"
}
