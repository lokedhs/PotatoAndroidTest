package com.dhsdevelopments.potato.service

import java.io.InterruptedIOException

internal class ReceiverStoppedException : Exception {
    constructor() {
    }

    constructor(e: InterruptedIOException) : super(e) {
    }
}
