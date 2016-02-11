package com.dhsdevelopments.potato.clientapi.message

class MessageElementParagraph(content: MessageElement) : TypedMessageElement(content) {

    override fun makeSpan(): CharSequence {
        return content.makeSpan()
    }
}
