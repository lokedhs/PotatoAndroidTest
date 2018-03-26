package com.dhsdevelopments.potato.channellist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import java.util.*

class ChannelListAdapter(private val parent: ChannelListActivity) : RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CHANNEL = 1
    }

    private var publicChannels: MutableList<ChannelEntry> = ArrayList()
    private var privateChannels: MutableList<ChannelEntry> = ArrayList()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(layoutInflater.inflate(R.layout.channel_list_header, parent, false))
            VIEW_TYPE_CHANNEL -> ChannelViewHolder(layoutInflater.inflate(R.layout.channel_list_content, parent, false))
            else -> throw RuntimeException("Unexpected view type=$viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            publicChannels.isEmpty() -> if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_CHANNEL
            position == 0 || !privateChannels.isEmpty() && position == publicChannels.size + 1 -> VIEW_TYPE_HEADER
            else -> VIEW_TYPE_CHANNEL
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when {
            publicChannels.isEmpty() -> {
                if (position == 0) {
                    (holder as HeaderViewHolder).setTitle("Conversations")
                }
                else {
                    (holder as ChannelViewHolder).fillInChannelEntry(privateChannels[position - 1])
                }
            }
            position == 0 -> (holder as HeaderViewHolder).setTitle("Channels")
            position < publicChannels.size + 1 -> (holder as ChannelViewHolder).fillInChannelEntry(publicChannels[position - 1])
            !privateChannels.isEmpty() -> {
                if (position == publicChannels.size + 1) {
                    (holder as HeaderViewHolder).setTitle("Conversations")
                }
                else {
                    (holder as ChannelViewHolder).fillInChannelEntry(privateChannels[position - publicChannels.size - 2])
                }
            }
        }

    }

    override fun getItemCount(): Int {
        var total = 0
        if (!publicChannels.isEmpty()) {
            total += publicChannels.size + 1
        }
        if (!privateChannels.isEmpty()) {
            total += privateChannels.size + 1
        }
        return total
    }

    fun selectDomain(domainId: String?) {
        publicChannels.clear()
        privateChannels.clear()

        if (domainId != null) {
            val db = PotatoApplication.getInstance(parent).cacheDatabase
            db.channelDao().findByDomain(domainId).forEach { channel ->
                val e = ChannelEntry(channel.id, channel.name, channel.privateUser != null, channel.unreadCount)
                if (e.isPrivateChannel) {
                    privateChannels.add(e)
                }
                else {
                    publicChannels.add(e)
                }
            }
        }

        notifyDataSetChanged()
    }

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class HeaderViewHolder(view: View) : ViewHolder(view) {
        private val titleView = view.findViewById<TextView>(R.id.header_title_text)

        fun setTitle(title: String) {
            titleView.text = title
        }
    }

    inner class ChannelViewHolder(private val view: View) : ViewHolder(view) {
        private val contentView: TextView = view.findViewById(R.id.content)
        private val unreadView: TextView = view.findViewById(R.id.unread_messages)

        fun fillInChannelEntry(item: ChannelEntry) {
            contentView.text = item.name
            if (item.unread > 0) {
                unreadView.text = item.unread.toString()
                unreadView.visibility = View.VISIBLE
            }
            else {
                unreadView.visibility = View.GONE
            }

            view.setOnClickListener { parent.setActiveChannel(item.id) }
        }
    }
}
