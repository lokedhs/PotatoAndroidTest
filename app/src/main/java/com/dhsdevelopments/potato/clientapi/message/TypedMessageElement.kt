package com.dhsdevelopments.potato.clientapi.message

open class TypedMessageElement(protected var content: MessageElement) : MessageElement() {

    override fun toString(): String {
        return "TypedMessageElement[type=" + javaClass.name +
                ", content=" + content +
                "] " + super.toString()
    }
}
