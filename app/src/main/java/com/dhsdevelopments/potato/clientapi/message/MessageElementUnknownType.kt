package com.dhsdevelopments.potato.clientapi.message

class MessageElementUnknownType(private val type: String) : MessageElement() {

    override fun makeSpan(): CharSequence {
        return "[TYPE=$type]"
    }

    override fun toString(): String {
        return "MessageElementUnknownType[type='$type']"
    }
}
