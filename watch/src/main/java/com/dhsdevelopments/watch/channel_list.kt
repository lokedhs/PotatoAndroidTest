package com.dhsdevelopments.watch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import android.support.wearable.activity.WearableActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.common.RemoteRequestService

class WearChannelListActivity : WearableActivity() {

    private val channelList by lazy { findViewById<RecyclerView>(R.id.channel_list_recycler_view) }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleBroadcastMessage(intent)
        }
    }

    private lateinit var channelListAdapter: WearChannelListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_channel_list)

        setAmbientEnabled()

        Log.d("Setting channelListAdapter")
        channelListAdapter = WearChannelListAdapter(this)
        channelList.adapter = channelListAdapter
    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter().apply {
            addAction(RemoteRequestService.ACTION_CHANNEL_LIST_UPDATED)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        if (PotatoWatchApplication.getInstance(this).hasUserData) {
            RemoteRequestService.loadChannelList(this)
        }
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onStop()
    }

    private fun handleBroadcastMessage(intent: Intent) {
        when (intent.action) {
            RemoteRequestService.ACTION_CHANNEL_LIST_UPDATED -> channelListAdapter.loadChannels()
        }
    }
}

data class ChannelEntry(val channelId: String, val name: String)

class WearChannelListAdapter(private val context: Context) : RecyclerView.Adapter<WearChannelListAdapter.ViewHolder>() {

    private val channels: MutableList<ChannelEntry> = ArrayList()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        loadChannels()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        (holder as ViewHolder).fillInView(channels[position])
    }

    override fun getItemCount(): Int {
        Log.d("Getting item count: ${channels.size}")
        return channels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.wear_channel_list_entry, parent, false))
    }

    fun loadChannels() {
        channels.clear()
        val db = PotatoWatchApplication.getInstance(context).cacheDatabase
        channels.addAll(db.channelDao().findAll().map { ChannelEntry(it.id, it.name) })
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameView = itemView.findViewById<TextView>(R.id.channel_name)

        fun fillInView(e: ChannelEntry) {
            channelNameView.text = e.name
        }
    }

}
