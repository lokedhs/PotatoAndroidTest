package com.dhsdevelopments.potato.editor

import android.widget.Filter
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

import java.text.Collator
import java.util.*

internal class UserNameSuggestFilter(private val usersTracker: ChannelUsersTracker,
                                     private val userNameSuggestAdapter: UserNameSuggestAdapter) : Filter() {

    private val listener: UserTrackerListener
    private var users: List<UserSuggestion>? = null
    private val userSuggestionComparator: Comparator<UserSuggestion>

    init {
        val collator = Collator.getInstance()
        userSuggestionComparator = Comparator<UserSuggestion> { o1, o2 -> collator.compare(o1.name, o2.name) }

        listener = UserTrackerListener()
        usersTracker.addUserActivityListener(listener)
    }

    override fun performFiltering(text: CharSequence?): Filter.FilterResults? {
        if (text == null) {
            return null
        }

        if (text[0] != '@') {
            Log.w("Attempt to filter a string which does not start with @: \"" + text + "\"")
            return null
        }

        if (text.length < 2) {
            return null
        }

        val textLower = text.toString().substring(1).toLowerCase(Locale.getDefault())
        val result = ArrayList<UserSuggestion>()
        for (s in getUsers()) {
            val parts = s.name.toLowerCase(Locale.getDefault()).split("\\W+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                if (part.startsWith(textLower)) {
                    result.add(s)
                    break
                }
            }
        }

        val filterResults = Filter.FilterResults()
        filterResults.values = result
        filterResults.count = result.size
        return filterResults
    }

    @SuppressWarnings("unchecked")
    override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults?) {
        if (filterResults != null) {
            @Suppress("UNCHECKED_CAST")
            userNameSuggestAdapter.setSuggestionList(filterResults.values as List<UserSuggestion>)
        }
    }

    @Synchronized private fun getUsers(): List<UserSuggestion> {
        if (users == null) {
            val userlist = usersTracker.getUsers()
            users = userlist.entries.map { (key, value) -> UserSuggestion(key, value.name) }.sortedWith(userSuggestionComparator)
        }
        return users!!
    }

    @Synchronized private fun clearUserList() {
        users = null
    }

    fun shutdown() {
        usersTracker.removeUserActivityListener(listener)
    }

    private inner class UserTrackerListener : ChannelUsersTracker.UserActivityListener {
        override fun activeUserListSync() {
            clearUserList()
        }

        override fun userUpdated(uid: String) {
            clearUserList()
        }
    }
}
