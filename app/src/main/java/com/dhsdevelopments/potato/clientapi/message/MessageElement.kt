package com.dhsdevelopments.potato.clientapi.message

import java.io.Serializable

abstract class MessageElement : Serializable {
    open fun makeSpan(): CharSequence {
        return "[NOT-IMPLEMENTED type=" + javaClass.name + "]"
    }
}
