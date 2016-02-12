package com.dhsdevelopments.potato.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.R

class SearchActivity : AppCompatActivity() {
    companion object {
        val EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        Log.d("Search activity started. intent=" + intent)
        if(intent.action != Intent.ACTION_SEARCH) {
            throw IllegalArgumentException("Search acivity not started with ACTION_SEARCH")
        }

        val query = intent.getStringExtra(SearchManager.QUERY)
        Log.d("Search query: '$query'")
    }
}
