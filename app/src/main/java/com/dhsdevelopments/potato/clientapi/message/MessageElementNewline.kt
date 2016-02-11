package com.dhsdevelopments.potato.clientapi.message

class MessageElementNewline : MessageElement() {
    override fun makeSpan(): CharSequence {
        return "\n"
    }
}
