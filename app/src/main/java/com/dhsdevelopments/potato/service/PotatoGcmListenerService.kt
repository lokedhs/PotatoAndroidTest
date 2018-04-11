package com.dhsdevelopments.potato.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channellist.ChannelListActivity
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity
import com.dhsdevelopments.potato.channelmessages.ChannelContentFragment
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.common.PotatoDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PotatoGcmListenerService : FirebaseMessagingService() {

    companion object {
        private const val UNREAD_NOTIFICATIONS_TAG = "unread_channels"
        private const val MESSAGE_NOTIFICATION_ID = 0
        private const val UNREAD_NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PotatoGcmListenerService created")
    }

    override fun onDestroy() {
        Log.d("PotatoGcmListenerService destroyed")
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val from = message.from
        val data = message.data
        Log.d("GCM message. from=$from, data=$data")

        val messageType = data["potato_message_type"]
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

    private fun processMessage(data: Map<String,String>) {
        //val messageId = data.getString("message_id")
        val notificationType = data["notification_type"]
        val text = data["text"]
        //val senderId = data.getString("sender_id")
        val senderName = data["sender_name"]
        val channelId = data["channel"]

        if(notificationType == null || text == null || senderName == null || channelId == null) {
            Log.e("Missing data in incoming new message notification: $data")
            return
        }

        val config = object : NotificationConfigProvider {
            override val enabledKey = getString(R.string.pref_notifications_private_message)
            override val vibrateKey = getString(R.string.pref_notifications_private_message_vibrate)
            override val ringtoneKey = getString(R.string.pref_notifications_private_message_ringtone)
        }
        sendNotification(null, MESSAGE_NOTIFICATION_ID, config) { builder ->
            val intent = Intent(this, ChannelContentActivity::class.java)
            intent.putExtra(ChannelContentFragment.ARG_CHANNEL_ID, channelId)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            builder
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle(when (notificationType) {
                        "PRIVATE" -> "Private message from $senderName"
                        "MENTION" -> "$senderName mentioned you"
                        "WORD" -> "Keyword mentioned from $senderName"
                        else -> "Notification in channel from $senderName"
                    })
                    .setContentText(text).setAutoCancel(true)
                    .setContentIntent(pendingIntent)
        }
    }

    private fun processUnread(data: Map<String,String>) {
        val cid = data["channel"]
        val unreadCountString = data["unread"]

        if(cid == null || unreadCountString == null) {
            Log.e("Missing data in unread notification: $data")
            return
        }

        val unreadCount = Integer.parseInt(unreadCountString)
        Log.d("Got unread notification: cid=$cid, unreadCount=$unreadCount")

        val db = PotatoApplication.getInstance(this).cacheDatabase
        val channel = db.channelDao().findById(cid)
        if(channel != null) {
            channel.unreadCount = unreadCount
            db.channelDao().updateChannel(channel)
            sendUnreadNotification(db)
        }
    }

    private fun sendUnreadNotification(db: PotatoDatabase) {
        db.channelDao().findUnread().forEach { channel ->
            val unread = channel.unreadCount
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (unread == 0) {
                mgr.cancel(UNREAD_NOTIFICATIONS_TAG, UNREAD_NOTIFICATION_ID)
            }
            else {
                val config = object : NotificationConfigProvider {
                    override val enabledKey = getString(R.string.pref_notifications_unread)
                    override val vibrateKey = getString(R.string.pref_notifications_unread_vibrate)
                    override val ringtoneKey = getString(R.string.pref_notifications_unread_ringtone)
                }
                sendNotification(UNREAD_NOTIFICATIONS_TAG, UNREAD_NOTIFICATION_ID, config) { builder ->
                    val intent = Intent(this, ChannelListActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT)
                    builder
                            .setSmallIcon(android.R.drawable.ic_dialog_email)
                            .setContentTitle("New Potato messages")
                            .setContentText("You have new messages in $unread channel" + if (unread == 1) "" else "s")
                            .setContentIntent(pendingIntent)

                }
            }
        }
    }

    private fun sendNotification(tag: String?, id: Int, config: NotificationConfigProvider, callback: (Notification.Builder) -> Unit) {
        val prefs = getSharedPreferences("com.dhsdevelopments.potato_preferences", MODE_PRIVATE)
        if (prefs.getBoolean(config.enabledKey, true)) {
            val builder = Notification.Builder(this)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)

            if (prefs.getBoolean(config.vibrateKey, true)) {
                builder.setVibrate(longArrayOf(0, 500, 0, 500))
            }

            val ringtone = prefs.getString(config.ringtoneKey, null)
            if (ringtone != null) {
                builder.setSound(Uri.parse(ringtone))
            }

            callback(builder)

            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.notify(tag, id, builder.build())
        }
    }

    interface NotificationConfigProvider {
        val enabledKey: String
        val vibrateKey: String
        val ringtoneKey: String
    }
}
