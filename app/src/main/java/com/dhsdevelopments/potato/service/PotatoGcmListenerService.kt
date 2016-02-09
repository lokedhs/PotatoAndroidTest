package com.dhsdevelopments.potato.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.StorageHelper
import com.dhsdevelopments.potato.channellist.ChannelListActivity
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity
import com.dhsdevelopments.potato.channelmessages.ChannelContentFragment
import com.google.android.gms.gcm.GcmListenerService

class PotatoGcmListenerService : GcmListenerService() {

    companion object {
        private val UNREAD_NOTIFICATIONS_TAG = "unread_channels"
        private val MESSAGE_NOTIFICATION_ID = 0
        private val UNREAD_NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PotatoGcmListenerService created")
    }

    override fun onDestroy() {
        Log.d("PotatoGcmListenerService destroyed")
        super.onDestroy()
    }

    override fun onMessageReceived(from: String?, data: Bundle?) {
        Log.d("GCM message. from=$from, data=$data")

        val messageType = data!!.getString("potato_message_type")
        if (messageType == null) {
            Log.e("Missing message_type in notification")
        }
        else {
            when (messageType) {
                "message" -> processMessage(data)
                "unread" -> processUnread(data)
            }
        }
    }

    private fun processMessage(data: Bundle) {
        val messageId = data.getString("message_id")
        val notificationType = data.getString("notification_type")
        val text = data.getString("text")
        val senderId = data.getString("sender_id")
        val senderName = data.getString("sender_name")
        val channelId = data.getString("channel")

        val intent = Intent(this, ChannelContentActivity::class.java)
        intent.putExtra(ChannelContentFragment.ARG_CHANNEL_ID, channelId)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val builder = android.support.v7.app.NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Message from " + senderName).setContentText(text).setAutoCancel(true).setContentIntent(pendingIntent)

        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(MESSAGE_NOTIFICATION_ID, builder.build())
    }

    private fun processUnread(data: Bundle) {
        val cid = data.getString("channel")
        val unreadCount = Integer.parseInt(data.getString("unread"))
        Log.d("Got unread notification: cid=$cid, unreadCount=$unreadCount")

        val db = PotatoApplication.getInstance(this).cacheDatabase
        val values = ContentValues()
        values.put(StorageHelper.CHANNELS_UNREAD, unreadCount)
        val res = db.update(StorageHelper.CHANNELS_TABLE, values, StorageHelper.CHANNELS_ID + " = ?", arrayOf(cid))
        if (res > 0) {
            sendUnreadNotification(db)
        }
    }

    private fun sendUnreadNotification(db: SQLiteDatabase) {
        db.query(StorageHelper.CHANNELS_TABLE,
                arrayOf("count(*)"),
                StorageHelper.CHANNELS_UNREAD + " > ?", arrayOf("0"),
                null, null, null, null).use { result ->
            if (!result.moveToNext()) {
                Log.e("No result when loading number of unrad channels")
                return
            }
            val unread = result.getInt(0)
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (unread == 0) {
                mgr.cancel(UNREAD_NOTIFICATIONS_TAG, UNREAD_NOTIFICATION_ID)
            }
            else {
                val intent = Intent(this, ChannelListActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)
                val builder = NotificationCompat.Builder(this).setSmallIcon(android.R.drawable.ic_dialog_email).setContentTitle("New Potato messages").setContentText("You have new messages in " + unread + " channel" + if (unread == 1) "" else "s").setAutoCancel(true).setContentIntent(pendingIntent)
                mgr.notify(UNREAD_NOTIFICATIONS_TAG, UNREAD_NOTIFICATION_ID, builder.build())
            }
        }
    }
}
