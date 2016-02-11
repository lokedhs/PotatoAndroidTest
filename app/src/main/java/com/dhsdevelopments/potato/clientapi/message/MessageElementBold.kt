package com.dhsdevelopments.potato.clientapi.message

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

class MessageElementBold(content: MessageElement) : TypedMessageElement(content) {

    override fun makeSpan(): CharSequence {
        val s = SpannableString(content.makeSpan())
        s.setSpan(StyleSpan(Typeface.BOLD), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}
