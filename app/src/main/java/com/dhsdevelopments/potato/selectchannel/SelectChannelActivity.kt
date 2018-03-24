package com.dhsdevelopments.potato.selectchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.Button
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.common.DbTools
import com.dhsdevelopments.potato.common.IntentUtil
import com.dhsdevelopments.potato.createchannel.CreateChannelActivity

class SelectChannelActivity : Activity() {

    companion object {
        val RESULT_ERROR_LOADING_CHANNEL = RESULT_FIRST_USER
    }

    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.channel_select_list) }
    private val createChannelButton by lazy { findViewById<Button>(R.id.channel_select_new_channel) }

    private lateinit var domainId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        title = "Available channels"

        domainId = intent.getStringExtra(IntentUtil.EXTRA_DOMAIN_ID)

        recyclerView.adapter = AvailableChannelListAdapter(this, domainId)
        createChannelButton.setOnClickListener { createChannelClicked() }
    }

    private fun createChannelClicked() {
        startActivity(Intent(this, CreateChannelActivity::class.java).apply {putExtra(CreateChannelActivity.EXTRA_DOMAIN_ID, domainId)})
    }

    fun channelSelected(channel: AvailableChannel) {
        DbTools.refreshChannelEntryInDb(this, channel.id,
                { message -> returnError(message) },
                { returnChannel(channel) })
    }

    private fun returnChannel(channel: AvailableChannel) {
        val intent = Intent().apply {
            putExtra(IntentUtil.EXTRA_CHANNEL_ID, channel.id)
            putExtra(IntentUtil.EXTRA_CHANNEL_NAME, channel.name)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun returnError(message: CharSequence) {
        val intent = Intent().apply {
            putExtra(IntentUtil.EXTRA_ERROR_MESSAGE, message)
        }
        setResult(RESULT_ERROR_LOADING_CHANNEL, intent)
        finish()
    }
}
