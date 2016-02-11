package com.dhsdevelopments.potato.clientapi.message

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan

class MessageElementUser(private val userId: String, private val userDescription: String) : MessageElement() {

    override fun makeSpan(): CharSequence {
        val s = SpannableString(userDescription)
        s.setSpan(BackgroundColorSpan(Color.rgb(0xe3, 0xe3, 0xe3)), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}
