package com.dhsdevelopments.potato.selectchannel

import com.dhsdevelopments.potato.clientapi.domainchannels.Channel
import java.text.Collator
import java.util.*

class AvailableChannel(channel: Channel) {
    object COMPARATOR: Comparator<AvailableChannel> {
        val collator = Collator.getInstance()

        override fun compare(p0: AvailableChannel?, p1: AvailableChannel?): Int {
            return collator.compare(p0!!.name, p1!!.name)
        }
    }

    val id = channel.id
    val name = channel.name
}
