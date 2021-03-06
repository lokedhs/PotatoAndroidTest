package com.dhsdevelopments.potato.userlist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channelmessages.HasChannelContentActivity
import com.dhsdevelopments.potato.clientapi.callServiceBackground
import com.dhsdevelopments.potato.clientapi.plainErrorHandler
import com.dhsdevelopments.potato.common.DbTools
import java.text.Collator
import java.util.*

class UserListAdapter(private val parentActivity: HasChannelContentActivity) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private val activeUsers = ArrayList<UserWrapper>()
    private val inactiveUsers = ArrayList<UserWrapper>()

    private var listener: ChannelUsersTracker.UserActivityListener = ActivityListener()
    private val comparator: Comparator<UserWrapper>

    init {
        val collator = Collator.getInstance()
        comparator = Comparator { o1, o2 -> collator.compare(o1.name, o2.name) }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        parentActivity.findUserTracker().addUserActivityListener(listener)
        loadStateFromUserTracker()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        parentActivity.findUserTracker().removeUserActivityListener(listener)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_list_header, parent, false))
            VIEW_TYPE_USER -> UserElementViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_list_element, parent, false))
            else -> throw IllegalArgumentException("Unexpected viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> {
                (holder as HeaderViewHolder).setHeaderTitle("Active")
            }
            activeUsers.size + 1 -> {
                (holder as HeaderViewHolder).setHeaderTitle("Inactive")
            }
            else -> {
                val activeLength = activeUsers.size
                val user = if (position <= activeLength) {
                    activeUsers[position - 1]
                }
                else {
                    inactiveUsers[position - activeLength - 2]
                }
                (holder as UserElementViewHolder).fillInUser(user)
            }
        }
    }

    override fun getItemCount(): Int = activeUsers.size + inactiveUsers.size + 2

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == activeUsers.size + 1) VIEW_TYPE_HEADER else VIEW_TYPE_USER
    }

    private fun loadStateFromUserTracker() {
        activeUsers.clear()
        inactiveUsers.clear()
        for ((uid, d) in parentActivity.findUserTracker().getUsers()) {
            val u = UserWrapper(uid, d.name)
            if (d.isActive) {
                activeUsers.add(u)
            }
            else {
                inactiveUsers.add(u)
            }
        }

        Collections.sort(activeUsers, comparator)
        Collections.sort(inactiveUsers, comparator)
        notifyDataSetChanged()
    }

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private inner class HeaderViewHolder(view: View) : ViewHolder(view) {
        private val headerText: TextView = view.findViewById(R.id.header_text)

        fun setHeaderTitle(title: String) {
            headerText.text = title
        }
    }

    private inner class UserElementViewHolder(itemView: View) : ViewHolder(itemView) {
        private var user: UserWrapper? = null
        private val userDescriptionView: TextView = itemView.findViewById(R.id.user_description_view)

        init {
            itemView.setOnClickListener {
                parentActivity.closeUserListDrawer()

                val context = parentActivity as Context
                val channelInfo = DbTools.loadChannelInfoFromDb(context, parentActivity.findUserTracker().cid)

                val uid = user!!.id
                val app = PotatoApplication.getInstance(context)
                callServiceBackground(app.findApiProvider().makePotatoApi().findPrivateChannelId(app.findApiKey(), channelInfo.domainId, uid), ::plainErrorHandler) { result ->
                    val privChannelId = result.channel
                    DbTools.ensureChannelInfo(context, privChannelId) {
                        parentActivity.openChannel(privChannelId)
                    }
                }
            }
        }

        fun fillInUser(user: UserWrapper) {
            this.user = user
            userDescriptionView.text = user.name
        }
    }

    private inner class ActivityListener : ChannelUsersTracker.UserActivityListener {
        override fun activeUserListSync() {
            loadStateFromUserTracker()
        }

        override fun userUpdated(uid: String) {
            loadStateFromUserTracker()
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_USER = 1
    }
}
