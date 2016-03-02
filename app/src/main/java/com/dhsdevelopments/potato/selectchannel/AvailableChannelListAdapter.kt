package com.dhsdevelopments.potato.selectchannel

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.callServiceBackground
import com.dhsdevelopments.potato.clientapi.plainErrorHandler
import com.dhsdevelopments.potato.loadAllChannelIdsInDomain
import java.util.*

@Suppress("unused")
class AvailableChannelListAdapter(val parent: SelectChannelActivity, val domainId: String) : RecyclerView.Adapter<AvailableChannelListAdapter.ViewHolder>() {
    private val channels = ArrayList<AvailableChannel>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        loadChannels()
    }

    private fun loadChannels() {
        val app = PotatoApplication.getInstance(parent)
        val call = app.potatoApi.getAllChannelsInDomain(app.apiKey, domainId, "1", "1")
        callServiceBackground(call, ::plainErrorHandler) { result ->
            val chList = loadAllChannelIdsInDomain(parent, domainId)
            channels.clear()
            channels.addAll(result.groups!!.flatMap {
                it.channels!!
                    .map { AvailableChannel(it) } }
                    .filter { !chList.contains(it.id) }
                    .sortedWith(AvailableChannel.COMPARATOR))
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.available_channel_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fillInView(channels[position])
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView = view.findViewById(R.id.channel_name) as TextView
        private var channel: AvailableChannel? = null

        init {
            view.setOnClickListener {
                parent.channelSelected(channel!!)
            }
        }

        fun fillInView(availableChannel: AvailableChannel) {
            channel = availableChannel
            titleView.text = availableChannel.name
        }
    }
}
