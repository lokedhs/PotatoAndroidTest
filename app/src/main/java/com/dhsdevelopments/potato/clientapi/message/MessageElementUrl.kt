package com.dhsdevelopments.potato.clientapi.message

import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan

class MessageElementUrl(private val addr: String, private val description: String) : MessageElement() {


    override fun makeSpan(): CharSequence {
        val s = SpannableString(description)
        s.setSpan(URLSpan(addr), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }

    override fun toString(): String {
        return "MessageElementUrl[addr='$addr', description='$description']"
    }
}
