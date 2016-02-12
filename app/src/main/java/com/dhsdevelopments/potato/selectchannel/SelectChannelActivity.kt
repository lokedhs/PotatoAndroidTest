package com.dhsdevelopments.potato.selectchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import com.dhsdevelopments.potato.R

class SelectChannelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_channel)
        title = "Available channels"

        val domainId = intent.getStringExtra(EXTRA_DOMAIN_ID)

        val recyclerView = findViewById(R.id.channel_select_list) as RecyclerView
        recyclerView.adapter = AvailableChannelListAdapter(domainId)
    }

    companion object {
        val EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id"
    }
}
