package com.dhsdevelopments.potato.channelmessages

import com.dhsdevelopments.potato.DateHelper
import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageImage
import java.io.Serializable
import java.util.*

class MessageWrapper(val msg: Message, dateHelper: DateHelper) : Serializable, Comparable<MessageWrapper> {
    val createdDate: Date
    val createdDateFormatted: String
    var shouldDisplayHeader: Boolean = false

    val id: String
        get() = msg.id

    val sender: String
        get() = msg.from

    val senderName: String
        get() = msg.fromName

    val content: MessageElement
        get() = msg.text

    val extraHtml: String?
        get() = msg.extraHtml

    val image: MessageImage?
        get() = msg.messageImage

    val updated: Int
        get() = msg.updated ?: 0

    val updatedDate: String?
        get() = msg.updatedDate

    val useMath: Boolean
        get() = msg.useMath

    init {
        this.shouldDisplayHeader = true
        val date = dateHelper.parseDate(msg.createdDate)
        this.createdDate = date
        this.createdDateFormatted = dateHelper.formatDateTimeOutputFormat(date)
    }

    override fun compareTo(other: MessageWrapper): Int {
        return createdDate.compareTo(other.createdDate)
    }
}
