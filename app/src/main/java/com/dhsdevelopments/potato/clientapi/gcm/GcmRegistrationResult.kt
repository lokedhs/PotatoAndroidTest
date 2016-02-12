package com.dhsdevelopments.potato.clientapi.gcm

import com.google.gson.annotations.SerializedName

class GcmRegistrationResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("detail")
    var detail: String? = null
}
