package com.dhsdevelopments.potato.search

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.DateHelper
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.search.SearchResult
import com.dhsdevelopments.potato.clientapi.search.SearchResultMessage
import java.util.*

class SearchResultAdapter(private val parent: SearchActivity) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private val dateHelper = DateHelper()
    private val searchResults: MutableList<SearchResultMessage> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fillInMessage(searchResults[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.message_search_result, parent, false))
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    fun updateSearchResults(results: SearchResult) {
        searchResults.clear()
        searchResults.addAll(results.messages)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderView = itemView.findViewById<TextView>(R.id.sender)
        private val dateView = itemView.findViewById<TextView>(R.id.date)
        private val dateDetailView = itemView.findViewById<TextView>(R.id.date_detail)
        private val contentView = itemView.findViewById<TextView>(R.id.content)
        private val senderNicknameView = itemView.findViewById<TextView>(R.id.sender_nickname)

        fun fillInMessage(message: SearchResultMessage) {
            val timestamp = dateHelper.parseDate(message.createdDate)

            senderView.text = message.senderName
            contentView.text = Html.fromHtml(message.content)
            dateView.text = DateHelper.makeDateDiffString(parent, timestamp.time)
            dateDetailView.text = dateHelper.formatDateTimeOutputFormat(timestamp)
        }

    }
}
