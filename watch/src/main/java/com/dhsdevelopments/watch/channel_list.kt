package com.dhsdevelopments.watch

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.wearable.activity.WearableActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import android.net.Uri
import android.support.wearable.preference.PreferenceIconHelper
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.NodeApi
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

class WearChannelListActivity : WearableActivity() {

    private val channelList by lazy { findViewById<RecyclerView>(R.id.channel_list_recycler_view)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_channel_list)

        setAmbientEnabled()

        Log.d("Setting channelListAdapter")
        channelList.adapter = WearChannelListAdapter(this)
    }
}

class WearChannelListAdapter(val context: Context) : RecyclerView.Adapter<WearChannelListAdapter.ViewHolder>() {

    private val channels: List<String> = emptyList()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        loadChannels()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        (holder as ViewHolder).fillInView(channels[position])
    }

    override fun getItemCount(): Int {
        Log.d("Getting item count: ${channels.size}")
        return channels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.wear_channel_list_entry, parent, false))
    }

    private fun loadChannels() {
        testGetDefaults(context)
        var api = PotatoWatchApplication.getInstance(context).apiProvider.makePotatoApi()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameView = itemView.findViewById<TextView>(R.id.channel_name)

        fun fillInView(s: String) {
            channelNameView.text = s
        }
    }

}

fun testGetDefaults(context: Context) {
    var apiClient: GoogleApiClient? = null
    apiClient = GoogleApiClient.Builder(context)
            .addApi(Wearable.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnectionSuspended(p0: Int) {
                }

                override fun onConnected(p0: Bundle?) {
                    Wearable.DataApi.getDataItem(apiClient, Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path("/potato/domain").build()).setResultCallback { result ->
                        Log.i("Got result from generic data call: ${result.dataItem}")
                    }

                    Wearable.NodeApi.getLocalNode(apiClient).setResultCallback { nodeResult ->
                        val url = Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(nodeResult.node.id).path("/potato/domain").build()
                        val item = Wearable.DataApi.getDataItem(apiClient, url)
                        item.setResultCallback { result ->
                            Log.i("Got result from data api with node ${nodeResult.node.id}: ${result.dataItem}")
                        }
                    }
                }
            }).build()
    apiClient.connect()
}
