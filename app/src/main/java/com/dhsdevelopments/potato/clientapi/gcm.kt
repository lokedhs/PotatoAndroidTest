package com.dhsdevelopments.potato.clientapi.gcm

import com.google.gson.annotations.SerializedName

class GcmRegistrationRequest {
    @SerializedName("token")
    lateinit var token: String

    constructor() {
    }

    constructor(token: String) {
        this.token = token
    }
}

@Suppress("unused")
class GcmRegistrationResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("detail")
    var detail: String? = null
}
