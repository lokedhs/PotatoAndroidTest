package com.dhsdevelopments.potato.selectchannel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.dhsdevelopments.potato.*
import com.dhsdevelopments.potato.clientapi.channelinfo.LoadChannelInfoResult

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
        val app = PotatoApplication.getInstance(this)
        val call = app.potatoApi.loadChannelInfo(app.apiKey, channel.id)
        callServiceBackground(call, { returnError(it) }, { updateDatabase(it); returnChannel(channel) } )
    }

    private fun updateDatabase(c: LoadChannelInfoResult) {
        val db = PotatoApplication.getInstance(this).cacheDatabase
        db.beginTransaction()
        try {
            db.delete(StorageHelper.CHANNELS_TABLE, "${StorageHelper.CHANNELS_ID} = ?", arrayOf(c.id))
            insertChannelIntoChannelsTable(db, c.id, c.domainId, c.name, c.unreadCount, c.privateUserId, false)
            db.setTransactionSuccessful()
        }
        finally {
            db.endTransaction()
        }
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
