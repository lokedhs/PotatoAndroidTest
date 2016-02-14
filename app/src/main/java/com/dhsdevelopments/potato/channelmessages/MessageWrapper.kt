package com.dhsdevelopments.potato.channelmessages

import com.dhsdevelopments.potato.DateHelper
import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageImage
import java.io.Serializable
import java.util.*

class MessageWrapper : Serializable {
    val msg: Message
    val createdDate: Date
    val createdDateFormatted: String
    var isShouldDisplayHeader: Boolean = false

    constructor(msg: Message, dateHelper: DateHelper) {
        this.msg = msg
        this.isShouldDisplayHeader = true

        val date = dateHelper.parseDate(msg.createdDate)
        this.createdDate = date
        this.createdDateFormatted = dateHelper.formatDateTimeOutputFormat(date)
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
        get() = msg.text.makeSpan()

    val extraHtml: String?
        get() = msg.extraHtml

    val image: MessageImage?
        get() = msg.messageImage
}
