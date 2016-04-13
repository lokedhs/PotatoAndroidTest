package com.dhsdevelopments.potato.selectchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.Button
import com.dhsdevelopments.potato.*

class SelectChannelActivity : Activity() {

    companion object {
        val RESULT_ERROR_LOADING_CHANNEL = RESULT_FIRST_USER
    }

    val recyclerView by nlazy { findViewById(R.id.channel_select_list) as RecyclerView }
    val createChannelButton by nlazy { findViewById(R.id.channel_select_new_channel) as Button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        title = "Available channels"

        val domainId = intent.getStringExtra(IntentUtil.EXTRA_DOMAIN_ID)

        recyclerView.adapter = AvailableChannelListAdapter(this, domainId)
        createChannelButton.setOnClickListener { createChannelClicked() }
    }

    private fun createChannelClicked() {
        Log.d("Create channel clicked")
    }

    fun channelSelected(channel: AvailableChannel) {
        DbTools.refreshChannelEntryInDb(this, channel.id,
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
        val intent = Intent()
        intent.putExtra(IntentUtil.EXTRA_ERROR_MESSAGE, message)
        setResult(RESULT_ERROR_LOADING_CHANNEL, intent)
        finish()
    }
}
