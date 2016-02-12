package com.dhsdevelopments.potato.search

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dhsdevelopments.potato.R

class SearchActivity : AppCompatActivity() {
    companion object {
        val EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }
}
