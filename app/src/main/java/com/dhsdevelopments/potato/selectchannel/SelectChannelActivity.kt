package com.dhsdevelopments.potato.selectchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.dhsdevelopments.potato.IntentUtil
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.nlazy
import com.dhsdevelopments.potato.refreshChannelEntryInDb

class SelectChannelActivity : Activity() {

    val recyclerView by nlazy { findViewById(R.id.channel_select_list) as RecyclerView }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        title = "Available channels"

        val domainId = intent.getStringExtra(IntentUtil.EXTRA_DOMAIN_ID)

        recyclerView.adapter = AvailableChannelListAdapter(this, domainId)
    }

    fun channelSelected(channel: AvailableChannel) {
        refreshChannelEntryInDb(this, channel.id,
                { message -> returnError(message) },
                { returnChannel(channel) })
    }

    private fun returnChannel(channel: AvailableChannel) {
        val intent = Intent()
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, channel.id)
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_NAME, channel.name)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun returnError(message: CharSequence) {
        throw RuntimeException("Got error: $message")
    }
}
