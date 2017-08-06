package com.dhsdevelopments.potato.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

class UserNameSuggestAdapter(context: Context, usersTracker: ChannelUsersTracker) : BaseAdapter(), Filterable {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var users: List<UserSuggestion> = emptyList()
    private val filter: UserNameSuggestFilter = UserNameSuggestFilter(usersTracker, this)

    fun shutdown() {
        filter.shutdown()
    }

    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(position: Int): Any {
        return users[position].id
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v =
                if (convertView != null && convertView.id == R.id.user_name_suggest_line_view) {
                    convertView
                }
                else {
                    inflater.inflate(R.layout.user_name_suggest_line, parent, false)
                }

        val userSuggestion = users[position]
        val textView = v.findViewById<TextView>(R.id.user_name_suggest_name)
        textView.text = userSuggestion.name
        return v
    }

    override fun getFilter(): Filter {
        return filter
    }

    internal fun setSuggestionList(users: List<UserSuggestion>) {
        this.users = users
        notifyDataSetChanged()
    }
}
