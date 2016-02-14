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
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

class SearchResultAdapter(private val parent: SearchActivity): RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private val dateFormat: MessageFormat
    private val isoDateFormat: SimpleDateFormat

    private val searchResults: MutableList<SearchResultMessage> = ArrayList()

    init {
        dateFormat = MessageFormat(parent.resources.getString(R.string.message_entry_date_label))
        isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

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
        private val senderView: TextView
        private val dateView: TextView
        private val dateDetailView: TextView
        private val contentView: TextView
        private val senderNicknameView: TextView

        init {
            senderView = itemView.findViewById(R.id.sender) as TextView
            dateView = itemView.findViewById(R.id.date) as TextView
            dateDetailView = itemView.findViewById(R.id.date_detail) as TextView
            contentView = itemView.findViewById(R.id.content) as TextView
            senderNicknameView = itemView.findViewById(R.id.sender_nickname) as TextView
        }

        fun fillInMessage(message: SearchResultMessage) {
            val timestamp = isoDateFormat.parse(message.createdDate)

            senderView.text = message.senderName
            contentView.text = Html.fromHtml(message.content)
            dateView.text = DateHelper.makeDateDiffString(parent, timestamp.time)
            dateDetailView.text = dateFormat.format(arrayOf(timestamp))
        }

    }
}