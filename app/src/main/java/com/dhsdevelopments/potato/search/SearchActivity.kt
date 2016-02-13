package com.dhsdevelopments.potato.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.search.SearchResult
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit

class SearchActivity : AppCompatActivity() {
    companion object {
        val EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        Log.d("Search activity started. intent=" + intent)
        if(intent.action != Intent.ACTION_SEARCH) {
            throw IllegalArgumentException("Search activity not started with ACTION_SEARCH")
        }

        val query = intent.getStringExtra(SearchManager.QUERY)
        Log.d("Search query: '$query'")
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)

        val app = PotatoApplication.getInstance(this)
        val call = app.potatoApi.searchMessages(app.apiKey, channelId, query, "0")
        call.enqueue(object: Callback<SearchResult?> {
            override fun onResponse(result: Response<SearchResult?>, retrofit: Retrofit) {
                if(result.isSuccess) {
                    processResults(result.body()!!)
                }
                else {
                    Log.e("Error when loading search result. code=${result.code()}, message=${result.message()}")
                }
            }

            override fun onFailure(result: Throwable?) {
                throw UnsupportedOperationException()
            }
        })
    }

    private fun processResults(body: SearchResult) {
        Log.i("got ${body.numFound} messages:")
        for(msg in body.messages) {
            Log.i("id=${msg.id}, content=${msg.content}")
        }
    }
}
