package com.dhsdevelopments.potato.clientapi.message

import android.text.Spannable
import android.text.SpannableString
import com.dhsdevelopments.potato.CodeTypefaceSpan

class MessageElementCode(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(): CharSequence {
        val s = SpannableString(content.makeSpan())
        s.setSpan(CodeTypefaceSpan(), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}
