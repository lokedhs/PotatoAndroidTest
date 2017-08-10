package com.dhsdevelopments.watch

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.wearable.activity.WearableActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class WearChannelListActivity() : WearableActivity() {

    val channelList by lazy { findViewById<RecyclerView>(R.id.channel_list_recycler_view)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_channel_list)

        setAmbientEnabled()

        Log.i("Setting channelListAdapter")
        channelList.adapter = WearChannelListAdapter()
    }
}

class WearChannelListAdapter : RecyclerView.Adapter<WearChannelListAdapter.ViewHolder>() {
    private val channels = listOf("Foo", "Bar", "Abctest", "Some more", "Another line")

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        (holder as ViewHolder).fillInView(channels[position])
    }

    override fun getItemCount(): Int {
        Log.i("Getting item count: ${channels.size}")
        return channels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.wear_channel_list_entry, parent, false))
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameView = itemView.findViewById<TextView>(R.id.channel_name)

        fun fillInView(s: String) {
            channelNameView.text = s
        }
    }

}
