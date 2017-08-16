package com.dhsdevelopments.watch

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import com.dhsdevelopments.potato.common.POTATO_CAPABILITY_NAME
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityApi
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable

class WatchTestActivity : WearableActivity() {

    private lateinit var apiClient: GoogleApiClient
    private var nodeId: String? = null

    private val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(connectionHint: Bundle?) {
            Log.i("onConnected called: $connectionHint")
            initNodes()
        }

        override fun onConnectionSuspended(cause: Int) {
            Log.i("onConnectionSuspended called: cause=$cause")
        }
    }

    private val connectionFailedListener = object : GoogleApiClient.OnConnectionFailedListener {
        override fun onConnectionFailed(result: ConnectionResult) {
            Log.i("onConnectionFailed called: result=$result")
        }
    }

    private val capabilityListener = object : CapabilityApi.CapabilityListener {
        override fun onCapabilityChanged(info: CapabilityInfo) {
            Log.i("Got capability: $info")
            selectActiveNode(info)
        }
    }

    private fun initNodes() {
        Log.i("Getting nodes")
        Wearable.CapabilityApi
                .getCapability(apiClient, POTATO_CAPABILITY_NAME, CapabilityApi.FILTER_REACHABLE)
                .setResultCallback { result ->
                    Log.i("Got nodes: ${result.capability}")
                    selectActiveNode(result.capability)
                }
    }

    private fun selectActiveNode(info: CapabilityInfo) {
        Log.i("selecting active node, name=${info.name}, nodeCount=${info.nodes.size}, nodes=${info.nodes}")
        val nodes = info.nodes
        nodeId = if (nodes.isEmpty()) null else nodes.iterator().next().id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_test)

        apiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build()
        apiClient.connect()

        Wearable.CapabilityApi.addCapabilityListener(apiClient, capabilityListener, POTATO_CAPABILITY_NAME)
    }

    override fun onDestroy() {
        apiClient.disconnect()
        super.onDestroy()
    }

    fun sendTestMessageClickHandler(@Suppress("UNUSED_PARAMETER") view: View) {
        Wearable.CapabilityApi.getAllCapabilities(apiClient, CapabilityApi.FILTER_ALL).setResultCallback { result ->
            Log.i("Got capabilities: ${result.status}, ${result.allCapabilities.size}")
            result.allCapabilities.forEach { (key, value) -> Log.i("Capability: $key = $value") }
        }

        Log.i("Sending test message, nodeId=$nodeId")
        if (nodeId != null) {
            Wearable.MessageApi.sendMessage(apiClient, nodeId, "/potato/foo", byteArrayOf(1, 2, 3, 4))
        }
    }
}
