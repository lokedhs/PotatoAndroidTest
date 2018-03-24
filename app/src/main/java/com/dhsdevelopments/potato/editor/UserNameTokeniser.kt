package com.dhsdevelopments.potato.editor

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.widget.MultiAutoCompleteTextView
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

class UserNameTokeniser(private val userTracker: ChannelUsersTracker) : MultiAutoCompleteTextView.Tokenizer {

    override fun findTokenStart(text: CharSequence, cursor: Int): Int {
        if (cursor == 0) {
            return cursor
        }

        var w = Character.offsetByCodePoints(text, cursor, -1)
        while (true) {
            val codePoint = Character.codePointAt(text, w)
            if (codePoint == '@'.toInt() && (w == 0 || Character.isSpaceChar(Character.codePointBefore(text, w)))) {
                return w
            }
            else if (!isTokenCharacter(codePoint)) {
                return cursor
            }

            if (w == 0) {
                break
            }

            w = Character.offsetByCodePoints(text, w, -1)
        }

        return cursor
    }

    private fun isTokenCharacter(codePoint: Int): Boolean {
        return Character.isLetterOrDigit(codePoint)
                || codePoint == '.'.toInt()
                || codePoint == '_'.toInt()
                || codePoint == '@'.toInt()
    }

    override fun findTokenEnd(text: CharSequence, cursor: Int): Int = cursor

    override fun terminateToken(text: CharSequence): CharSequence {
        val uid = text.toString()
        val u = userTracker.getUsers()[uid]
        val name = u?.name ?: uid

        val s = SpannableString(name)
        s.setSpan(BackgroundColorSpan(Color.rgb(210, 210, 210)), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(UidSpan(uid, name), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}
