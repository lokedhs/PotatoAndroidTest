package com.dhsdevelopments.potato.clientapi.message

import android.text.SpannableStringBuilder

class MessageElementList(private val list: List<MessageElement>) : MessageElement() {

    override fun makeSpan(): CharSequence {
        val builder = SpannableStringBuilder()
        for (element in list) {
            builder.append(element.makeSpan())
        }
        return builder
    }

    override fun toString(): String {
        return "MessageElementList[" +
                "list=" + list +
                "] " + super.toString()
    }
}
