package com.dhsdevelopments.potato.channelmessages

import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageImage
import java.io.Serializable
import java.text.DateFormat
import java.text.MessageFormat
import java.text.ParseException
import java.util.*

class MessageWrapper(msg: Message, isoDateFormat: DateFormat, dateFormat: MessageFormat) : Serializable {
    val msg: Message
    val createdDate: Date
    val createdDateFormatted: String
    var isShouldDisplayHeader: Boolean = false

    init {
        this.msg = msg
        this.isShouldDisplayHeader = true

        val date: Date
        try {
            date = isoDateFormat.parse(msg.createdDate)
        }
        catch (e: ParseException) {
            throw IllegalStateException("Unable to parse date format from server: '${msg.createdDate}'", e)
        }

        this.createdDate = date
        this.createdDateFormatted = dateFormat.format(arrayOf(date))
    }

    val id: String
        get() = msg.id

    val sender: String
        get() = msg.from

    val senderName: String
        get() = msg.fromName

    val content: MessageElement
        get() = msg.text

    val markupContent: CharSequence
        get() = msg.text.spannable

    val extraHtml: String?
        get() = msg.extraHtml

    val image: MessageImage?
        get() = msg.messageImage
}
