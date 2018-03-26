package com.dhsdevelopments.potato.createchannel

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.common.RemoteRequestService
import kotlinx.android.synthetic.main.activity_create_channel.*

class CreateChannelActivity : AppCompatActivity() {

    private val nameField by lazy { findViewById<EditText>(R.id.create_channel_name_field) }
    private val topicField by lazy { findViewById<EditText>(R.id.create_channel_topic_field) }

    private lateinit var domainId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        domainId = intent.getStringExtra(EXTRA_DOMAIN_ID)

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
                RemoteRequestService.createPublicChannel(this, domainId, nameField.text.toString().trim(), topicField.text.toString().trim())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_DOMAIN_ID = "domainId"
    }
}
