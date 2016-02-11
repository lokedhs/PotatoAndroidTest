package com.dhsdevelopments.potato.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

class UserNameSuggestAdapter(context: Context, usersTracker: ChannelUsersTracker) : BaseAdapter(), Filterable {
    private val inflater: LayoutInflater
    private var users: List<UserSuggestion>? = null
    private val filter: UserNameSuggestFilter

    init {
        filter = UserNameSuggestFilter(usersTracker, this)
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun shutdown() {
        filter.shutdown()
    }

    override fun getCount(): Int {
        return users!!.size
    }

    override fun getItem(position: Int): Any {
        return users!![position].id
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View
        if (convertView != null && convertView.id == R.id.user_name_suggest_line_view) {
            v = convertView
        }
        else {
            v = inflater.inflate(R.layout.user_name_suggest_line, parent, false)
        }

        val userSuggestion = users!![position]
        val textView = v.findViewById(R.id.user_name_suggest_name) as TextView
        textView.text = userSuggestion.name
        return v
    }

    override fun getFilter(): Filter {
        return filter
    }

    internal fun setSuggestionList(users: List<UserSuggestion>) {
        this.users = users
        Log.i("Setting suggestion list: " + users)
        notifyDataSetChanged()
    }
}
