package com.dhsdevelopments.potato.createchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.common.Log
import kotlinx.android.synthetic.main.activity_create_channel.*

class CreateChannelActivity : AppCompatActivity() {

    val nameField by lazy { findViewById<EditText>(R.id.create_channel_name_field) }
    val nicknameField by lazy { findViewById<EditText>(R.id.create_channel_name_field) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_channel)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_channel_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_option_create_channel -> {
                Log.i("Channel should be created here")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
