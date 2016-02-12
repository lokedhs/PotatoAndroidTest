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
