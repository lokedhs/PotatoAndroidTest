package com.dhsdevelopments.potato.clientapi.message

class MessageElementString(private val value: String) : MessageElement() {

    override fun makeSpan(): CharSequence {
        return value
    }

    override fun toString(): String {
        return "MessageElementString[" +
                "value='" + value + '\'' +
                "] " + super.toString()
    }
}
