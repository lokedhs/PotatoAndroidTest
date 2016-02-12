package com.dhsdevelopments.potato.selectchannel

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.R

class AvailableChannelListAdapter(domainId: String) : RecyclerView.Adapter<AvailableChannelListAdapter.ViewHolder>() {
    private val channels = emptyList<AvailableChannel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        return null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 0
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView

        init {
            this.titleView = view.findViewById(R.id.header_title_text) as TextView
            Log.i("Created header view. titleView=" + titleView)
        }

        fun setTitle(title: String) {
            titleView.text = title
        }
    }

}
