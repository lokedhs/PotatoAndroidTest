package com.dhsdevelopments.potato.channellist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.StorageHelper
import java.util.*

class ChannelListAdapter(private val parent: ChannelListActivity) : RecyclerView.Adapter<ChannelListAdapter.ViewHolder>() {
    companion object {
        private val VIEW_TYPE_HEADER = 0
        private val VIEW_TYPE_CHANNEL = 1
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
            else -> throw RuntimeException("Unexpected view type=" + viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (publicChannels.isEmpty()) {
            return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_CHANNEL
        }
        else {
            if (position == 0 || (!privateChannels.isEmpty() && position == publicChannels.size + 1)) {
                return VIEW_TYPE_HEADER
            }
            else {
                return VIEW_TYPE_CHANNEL
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (publicChannels.isEmpty()) {
            if (position == 0) {
                (holder as HeaderViewHolder).setTitle("Conversations")
            }
            else {
                (holder as ChannelViewHolder).fillInChannelEntry(privateChannels[position - 1])
            }
        }
        else {
            if (position == 0) {
                (holder as HeaderViewHolder).setTitle("Channels")
            }
            else if (position < publicChannels.size + 1) {
                (holder as ChannelViewHolder).fillInChannelEntry(publicChannels[position - 1])
            }
            else if (!privateChannels.isEmpty()) {
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
            db.query(StorageHelper.CHANNELS_TABLE,
                    arrayOf(StorageHelper.CHANNELS_ID, StorageHelper.CHANNELS_NAME, StorageHelper.CHANNELS_PRIVATE, StorageHelper.CHANNELS_UNREAD),
                    StorageHelper.CHANNELS_DOMAIN + " = ?", arrayOf(domainId),
                    null, null, StorageHelper.CHANNELS_NAME, null).use { result ->
                while (result.moveToNext()) {
                    val cid = result.getString(0)
                    val name = result.getString(1)
                    val privateUser = result.getString(2)
                    val unread = result.getInt(3)
                    val e = ChannelEntry(cid, name, privateUser != null, unread)
                    if (e.isPrivateChannel) {
                        privateChannels.add(e)
                    }
                    else {
                        publicChannels.add(e)
                    }
                }
            }
        }

        notifyDataSetChanged()
    }

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class HeaderViewHolder(view: View) : ViewHolder(view) {
        private val titleView: TextView

        init {
            this.titleView = view.findViewById(R.id.header_title_text) as TextView
        }

        fun setTitle(title: String) {
            titleView.text = title
        }
    }

    inner class ChannelViewHolder(private val view: View) : ViewHolder(view) {
        private val contentView: TextView
        private val unreadView: TextView

        init {
            contentView = view.findViewById(R.id.content) as TextView
            unreadView = view.findViewById(R.id.unread_messages) as TextView
        }

        fun fillInChannelEntry(item: ChannelEntry) {
            contentView.text = item.name
            if (item.unread > 0) {
                unreadView.text = item.unread.toString()
                unreadView.visibility = View.VISIBLE
            }
            else {
                unreadView.visibility = View.GONE
            }

            view.setOnClickListener { v ->
                parent.setActiveChannel(item.id)
            }
        }
    }
}
