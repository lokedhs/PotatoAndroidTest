package com.dhsdevelopments.watch

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.dhsdevelopments.potato.common.APIKEY_DATA_MAP_PATH
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

fun testGetDefaults(context: Context) {
    var apiClient: GoogleApiClient? = null
    apiClient = GoogleApiClient.Builder(context)
            .addApi(Wearable.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnectionSuspended(p0: Int) {
                }

                override fun onConnected(p0: Bundle?) {
                    val url = Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(APIKEY_DATA_MAP_PATH).build()
                    Wearable.DataApi.getDataItem(apiClient, url)
                            .setResultCallback { result ->
                                Log.i("Got result from generic data call (url: $url): ${result.dataItem}")
                                printDataItemsFromAllNodes()
                            }
                }

                private fun printDataItemsFromAllNodes() {
                    Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback { allNodes ->
                        Log.i("Got ${allNodes.nodes.size} nodes")
                        allNodes.nodes.forEach { node ->
                            Log.i("  checking node ${node.displayName}/${node.id} ")
                            val url = Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(node.id).path(APIKEY_DATA_MAP_PATH).build()
                            Wearable.DataApi.getDataItem(apiClient, url).setResultCallback { item ->
                                Log.i("  got result from data api from url $url with node ${node.displayName}/${node.id}: ${item.dataItem}")
                                printPayload(item.dataItem?.data)
                                printAllDataItemsGeneric()
                            }
                        }
                    }
                }

                private fun printPayload(data: ByteArray?) {
                    if (data != null) {
                        val m = DataMap.fromByteArray(data)
                        Log.i("  payload = $m")
                    }
                }

                private fun printAllDataItemsGeneric() {
                    Log.d("Requesting all data items")
                    Wearable.DataApi.getDataItems(apiClient)
                            .setResultCallback { results ->
                                Log.d("Global data item request result. num items = ${results.count}")
                                results.forEach { item ->
                                    Log.d("  got item: ${item.uri}")
                                    printPayload(item.data)
                                    printSpecific()
                                }
                            }
                }

                private fun printSpecific() {
                    val url = Uri.parse("wear:/potato/apikey")
                    Log.d("Attempting to get specific item: $url")
                    Wearable.DataApi.getDataItems(apiClient, url)
                            .setResultCallback { items ->
                                Log.d("Result from specific item request: ${items.status}")
                                if (items.status.isSuccess) {
                                    items.forEach { item ->
                                        printPayload(item.data)
                                    }
                                }

                                val cl = apiClient
                                if (cl != null && cl.isConnected) {
                                    cl.disconnect()
                                }
                            }
                }
            }).build()
    apiClient.connect()
}
