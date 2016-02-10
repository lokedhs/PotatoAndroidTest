package com.dhsdevelopments.potato.channelmessages

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.TextView
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.imagecache.LoadImageCallback
import com.dhsdevelopments.potato.imagecache.StorageType
import com.dhsdevelopments.potato.nlazy
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

class MessageDetailActivity : AppCompatActivity() {

    private val senderImageView        by nlazy { findViewById(R.id.message_detail_sender_image) as ImageView }
    private val senderNameTextView     by nlazy { findViewById(R.id.message_detail_sender_name) as TextView }
    private val senderNicknameTextView by nlazy { findViewById(R.id.message_detail_sender_nickname) as TextView }
    private val sentDateTextView       by nlazy { findViewById(R.id.message_detail_sent_date) as TextView }

    private lateinit var imageCache: ImageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show() }

        val msg = intent.getSerializableExtra(EXTRA_MESSAGE_DATA) as MessageWrapper
        val userName = intent.getStringExtra(EXTRA_USER_NAME)
        val userNickname = intent.getStringExtra(EXTRA_USER_NICKNAME)

        imageCache = ImageCache(this)

        val res = resources
        imageCache.loadImageFromApi("/users/" + msg.sender + "/image",
                res.getDimensionPixelSize(R.dimen.message_details_sender_image_width),
                res.getDimensionPixelSize(R.dimen.message_details_sender_image_height),
                StorageType.SHORT,
                object: LoadImageCallback {
                    override fun bitmapLoaded(bitmap: Bitmap?) {
                        senderImageView.setImageDrawable(BitmapDrawable(res, bitmap))
                    }
                    override fun bitmapNotFound() {
                        Log.w("Unable to load bitmap")
                    }
                })
        senderNameTextView.text = userName
        senderNicknameTextView.text = userNickname
        sentDateTextView.text = DateUtils.formatDateTime(this, msg.createdDate.time, DateUtils.FORMAT_SHOW_TIME)
    }

    override fun onDestroy() {
        imageCache.close()
        super.onDestroy()
    }

    companion object {
        internal val EXTRA_MESSAGE_DATA = "com.dhsdevelopments.potato.MESSAGE_DATA"
        internal val EXTRA_USER_NAME = "com.dhsdevelopments.potato.USER_DESCRIPTION"
        internal val EXTRA_USER_NICKNAME = "com.dhsdevelopments.potato.USER_NICKNAME"

        fun makeIntent(context: Context, userTracker: ChannelUsersTracker, msg: MessageWrapper): Intent {
            val uid = msg.sender
            val intent = Intent(context, MessageDetailActivity::class.java)
            intent.putExtra(EXTRA_MESSAGE_DATA, msg)
            intent.putExtra(EXTRA_USER_NAME, userTracker.getNameForUid(uid))
            intent.putExtra(EXTRA_USER_NICKNAME, userTracker.getNicknameForUid(uid))
            return intent
        }
    }
}
