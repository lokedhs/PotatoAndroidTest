package com.dhsdevelopments.potato.clientapi.gcm

import com.google.gson.annotations.SerializedName

class GcmRegistrationRequest {
    @SerializedName("token")
    lateinit var token: String
    @SerializedName("provider")
    lateinit var provider: String

    constructor() {
    }

    constructor(token: String, provider: String) {
        this.token = token
        this.provider = provider
    }
}

@Suppress("unused")
class GcmRegistrationResult {
    @SerializedName("result")
    lateinit var result: String

    @SerializedName("detail")
    var detail: String? = null
}
