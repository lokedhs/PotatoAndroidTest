package com.dhsdevelopments.potato.channelmessages

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.common.DateHelper
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.common.RemoteRequestService
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.imagecache.LoadImageCallback
import com.dhsdevelopments.potato.imagecache.StorageType
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

class MessageDetailActivity : AppCompatActivity() {

    private val senderImageView by lazy { findViewById<ImageView>(R.id.message_detail_sender_image) }
    private val senderNameTextView by lazy { findViewById<TextView>(R.id.message_detail_sender_name) }
    private val senderNicknameTextView by lazy { findViewById<TextView>(R.id.message_detail_sender_nickname) }
    private val sentDateTextView by lazy { findViewById<TextView>(R.id.message_detail_sent_date) }
    private val editedInfo by lazy { findViewById<TextView>(R.id.message_detail_edited_ref) }
    private val messageText by lazy { findViewById<TextView>(R.id.message_detail_message_text) }

    private lateinit var msg: MessageWrapper
    private lateinit var imageCache: ImageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        title = "Details"

        val dateHelper = DateHelper()

        msg = intent.getSerializableExtra(EXTRA_MESSAGE_DATA) as MessageWrapper
        val userName = intent.getStringExtra(EXTRA_USER_NAME)
        val userNickname = intent.getStringExtra(EXTRA_USER_NICKNAME)

        imageCache = ImageCache(this)

        val res = resources
        imageCache.loadImageFromApi("/users/" + msg.sender + "/image",
                res.getDimensionPixelSize(R.dimen.message_details_sender_image_width),
                res.getDimensionPixelSize(R.dimen.message_details_sender_image_height),
                StorageType.SHORT,
                object : LoadImageCallback {
                    override fun bitmapLoaded(bitmap: Bitmap) {
                        senderImageView.setImageBitmap(bitmap)
                    }

                    override fun bitmapNotFound() {
                        Log.w("Unable to load bitmap")
                    }
                })
        senderNameTextView.text = userName
        senderNicknameTextView.text = userNickname
        sentDateTextView.text = dateHelper.formatDateTimeOutputFormat(msg.createdDate)
        editedInfo.text = msg.updatedDate ?: ""
        messageText.text = msg.content.makeSpan(MessageElement.SpanGenerationContext(this))
    }

    override fun onDestroy() {
        imageCache.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.message_detail_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_option_delete_message -> run { deleteMessage(); true }
            R.id.menu_option_edit_message -> run { editMessage(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteMessage() {
        AlertDialog.Builder(this).setMessage(R.string.delete_message_confirmation_text)
                .setPositiveButton(R.string.delete_message_confirm, { dialog, which ->
                    RemoteRequestService.deleteMessage(this, msg.id)
                    finish()
                })
                .setNegativeButton(R.string.delete_message_deny, null)
                .show()
    }

    private fun editMessage() {
        Log.e("Need to implement message editing")
    }

    companion object {
        internal const val EXTRA_MESSAGE_DATA = "com.dhsdevelopments.potato.MESSAGE_DATA"
        internal const val EXTRA_USER_NAME = "com.dhsdevelopments.potato.USER_DESCRIPTION"
        internal const val EXTRA_USER_NICKNAME = "com.dhsdevelopments.potato.USER_NICKNAME"

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
