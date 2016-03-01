package com.dhsdevelopments.potato.clientapi.leavechannel

import com.dhsdevelopments.potato.clientapi.RemoteResult

class LeaveChannelResult : RemoteResult {
    lateinit var result: String
    lateinit var detail: String

    override fun errorMsg(): String? {
        return if (result == "ok") null else result
    }
}
