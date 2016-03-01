package com.dhsdevelopments.potato.selectchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.dhsdevelopments.potato.IntentUtil
import com.dhsdevelopments.potato.R

class SelectChannelActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        title = "Available channels"

        val domainId = intent.getStringExtra(EXTRA_DOMAIN_ID)

        val recyclerView = findViewById(R.id.channel_select_list) as RecyclerView
        recyclerView.adapter = AvailableChannelListAdapter(this, domainId)
    }

    companion object {
        val EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id"
    }

    fun channelSelected(channel: AvailableChannel) {
        val intent = Intent()
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, channel.id)
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_NAME, channel.name)
        setResult(RESULT_OK, intent)
        finish()
    }
}
