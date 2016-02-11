package com.dhsdevelopments.potato.clientapi.notifications

import com.google.gson.annotations.SerializedName

import java.io.Serializable

open class PotatoNotification : Serializable {
    @SerializedName("type")
    lateinit var type: String


    override fun toString(): String {
        return "PotatoNotification[type='$type']"
    }
}
