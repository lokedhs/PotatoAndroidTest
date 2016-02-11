package com.dhsdevelopments.potato.clientapi.message

class MessageElementCodeBlock(private val language: String, private val code: String) : MessageElement() {

    override fun makeSpan(): CharSequence {
        return code
    }

    override fun toString(): String {
        return "MessageElementCodeBlock[" +
                "language='" + language + '\'' +
                ", code='" + code + '\'' +
                "] " + super.toString()
    }
}
