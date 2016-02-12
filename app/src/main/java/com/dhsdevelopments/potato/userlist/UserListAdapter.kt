package com.dhsdevelopments.potato.userlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.R

import java.text.Collator
import java.util.*

class UserListAdapter(private val userTracker: ChannelUsersTracker) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private val activeUsers = ArrayList<UserWrapper>()
    private val inactiveUsers = ArrayList<UserWrapper>()

    private var listener: ChannelUsersTracker.UserActivityListener = ActivityListener()
    private val comparator: Comparator<in UserWrapper>

    init {
        val collator = Collator.getInstance()
        comparator = Comparator<com.dhsdevelopments.potato.userlist.UserWrapper> { o1, o2 -> collator.compare(o1.name, o2.name) }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        userTracker.addUserActivityListener(listener)
        loadStateFromUserTracker()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        userTracker.removeUserActivityListener(listener)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_list_header, parent, false))
            VIEW_TYPE_USER -> UserElementViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_list_element, parent, false))
            else -> throw IllegalArgumentException("Unexpected viewType: " + viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            (holder as HeaderViewHolder).setHeaderTitle("Active")
        }
        else if (position == activeUsers.size + 1) {
            (holder as HeaderViewHolder).setHeaderTitle("Inactive")
        }
        else {
            val activeLength = activeUsers.size
            val user: UserWrapper
            if (position <= activeLength) {
                user = activeUsers[position - 1]
            }
            else {
                user = inactiveUsers[position - activeLength - 2]
            }
            (holder as UserElementViewHolder).fillInUser(user)
        }
    }

    override fun getItemCount(): Int {
        return activeUsers.size + inactiveUsers.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == activeUsers.size + 1) VIEW_TYPE_HEADER else VIEW_TYPE_USER
    }

    private fun loadStateFromUserTracker() {
        activeUsers.clear()
        inactiveUsers.clear()
        for ((uid, d) in userTracker.getUsers()) {
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
        private val headerText: TextView

        init {
            headerText = view.findViewById(R.id.header_text) as TextView
        }

        fun setHeaderTitle(title: String) {
            headerText.text = title
        }
    }

    private inner class UserElementViewHolder(itemView: View) : ViewHolder(itemView) {
        private var user: UserWrapper? = null
        private val userDescriptionView: TextView

        init {
            userDescriptionView = itemView.findViewById(R.id.user_description_view) as TextView
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
        private val VIEW_TYPE_HEADER = 0
        private val VIEW_TYPE_USER = 1
    }
}
