package com.dhsdevelopments.potato.common

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.support.v4.content.LocalBroadcastManager
import android.webkit.MimeTypeMap
import com.dhsdevelopments.potato.clientapi.ImageUriRequestBody
import com.dhsdevelopments.potato.clientapi.UpdateUnreadNotificationRequest
import com.dhsdevelopments.potato.clientapi.callService
import com.dhsdevelopments.potato.clientapi.channelinfo.CreateChannelRequest
import com.dhsdevelopments.potato.clientapi.command.SendCommandRequest
import com.dhsdevelopments.potato.clientapi.editchannel.UpdateChannelVisibilityRequest
import com.dhsdevelopments.potato.clientapi.plainErrorHandler
import com.dhsdevelopments.potato.clientapi.sendmessage.unreadnotification.SendMessageRequest
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import okhttp3.MultipartBody
import java.io.IOException

class RemoteRequestService : IntentService("RemoteRequestService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_MARK_NOTIFICATIONS -> markNotificationsForChannelImpl(intent.getStringExtra(EXTRA_CHANNEL_ID))
                ACTION_LOAD_CHANNEL_LIST -> loadChannelListImpl()
                ACTION_UPDATE_UNREAD_SUBSCRIPTION -> updateUnreadSubscriptionStateImpl(intent.getStringExtra(EXTRA_CHANNEL_ID), intent.getBooleanExtra(EXTRA_UPDATE_STATE, false))
                ACTION_SEND_MESSAGE_WITH_IMAGE -> sendMessageWithImageImpl(intent.getStringExtra(EXTRA_CHANNEL_ID), intent.getParcelableExtra<Parcelable>(EXTRA_IMAGE_URI) as Uri)
                ACTION_DELETE_MESSAGE -> deleteMessageImpl(intent.getStringExtra(EXTRA_MESSAGE_ID))
                ACTION_SEND_COMMAND -> sendCommandImpl(intent.getStringExtra(EXTRA_CHANNEL_ID), intent.getStringExtra(EXTRA_CMD), intent.getStringExtra(EXTRA_ARGS), intent.getBooleanExtra(EXTRA_REPLY, false))
                ACTION_LEAVE_CHANNEL -> leaveChannelImpl(intent.getStringExtra(EXTRA_CHANNEL_ID))
                ACTION_UPDATE_CHANNEL_VISIBILITY -> updateChannelVisibilityImpl(intent.getStringExtra(EXTRA_CHANNEL_ID), intent.getBooleanExtra(EXTRA_VISIBILITY, false))
                ACTION_CREATE_PUBLIC_CHANNEL -> createPublicChannelImpl(intent.getStringExtra(EXTRA_DOMAIN_ID), intent.getStringExtra(EXTRA_CHANNEL_NAME), intent.getStringExtra(EXTRA_CHANNEL_TOPIC))
            }
        }
    }

    private fun sendMessageWithImageImpl(cid: String, imageUri: Uri) {
        val app = CommonApplication.getInstance(this)

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(imageUri)) ?: "jpg"
        val call = app.findApiProvider()
                .makePotatoApi()
                .sendMessageWithFile(app.findApiKey(),
                                     cid,
                                     SendMessageRequest("Uploaded file from mobile"),
                                     MultipartBody.Part.createFormData("body", "file.$extension", ImageUriRequestBody(this, imageUri)))
        callService(call, ::plainErrorHandler) { response ->
            com.dhsdevelopments.potato.common.Log.i("Uploaded image, messageId=${response.id}")
        }
    }

    private fun markNotificationsForChannelImpl(cid: String) {
        val app = CommonApplication.getInstance(this)
        val call = app.findApiProvider().makePotatoApi().clearNotificationsForChannel(app.findApiKey(), cid)
        callService(call, ::plainErrorHandler) {
            com.dhsdevelopments.potato.common.Log.d("Notifications cleared for channel: $cid")
        }
    }

    private fun loadChannelListImpl() {
        var errorMessage: String? = null
        try {
            val app = CommonApplication.getInstance(this)
            val call = app.findApiProvider().makePotatoApi().getChannels2(app.findApiKey())
            val result = call.execute()

            if (result.isSuccessful) {
                val db = app.cacheDatabase
                db.runInTransaction {
                    // We need to delete everything from the table since this call returns the full state
                    db.deleteChannelsAndDomains()
                    result.body()!!.domains.filter { it.type != "PRIVATE" }.forEach { d ->
                        db.domainDao().insertDomain(DomainDescriptor(d.id, d.name))
                        for (c in d.channels) {
                            DbTools.insertChannelIntoChannelsTable(db, c.id, d.id, c.name, c.unreadCount, c.privateUser, c.hide)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("Exception when loading channels", e)
            errorMessage = e.message
        }

        val mgr = LocalBroadcastManager.getInstance(this)
        val intent: Intent
        if (errorMessage == null) {
            intent = Intent(ACTION_CHANNEL_LIST_UPDATED)
        } else {
            intent = Intent(ACTION_CHANNEL_LIST_UPDATE_FAIL)
            intent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
        }
        mgr.sendBroadcast(intent)
    }

    private fun updateUnreadSubscriptionStateImpl(cid: String, add: Boolean) {
        val app = CommonApplication.getInstance(this)
        val gcmSenderId = app.findGcmSenderId()
        if (gcmSenderId == "") {
            return
        }

        val token = InstanceID.getInstance(this).getToken(gcmSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
        val call = app.findApiProvider().makePotatoApi().updateUnreadNotification(app.findApiKey(), cid, UpdateUnreadNotificationRequest(token, add, "gcm"))
        callService(call, ::plainErrorHandler) {
            updateRegistrationInDb(cid, add)
            com.dhsdevelopments.potato.common.Log.d("Subscription updated successfully")
        }
    }

    private fun deleteMessageImpl(messageId: String) {
        val app = CommonApplication.getInstance(this)
        val call = app.findApiProvider().makePotatoApi().deleteMessage(app.findApiKey(), messageId)
        callService(call, ::plainErrorHandler) {
            com.dhsdevelopments.potato.common.Log.d("Message deleted successfully")
        }
    }

    private fun updateRegistrationInDb(cid: String, add: Boolean) {
        val db = CommonApplication.getInstance(this).cacheDatabase
        db.updateShowUnread(cid, add)
    }

    private fun sendCommandImpl(cid: String, cmd: String, args: String, reply: Boolean) {
        val app = CommonApplication.getInstance(this)
        Log.i("Command sent successfully, cid=$cid, cmd=$cmd")
        callService(app.findApiProvider().makePotatoApi().sendCommand(app.findApiKey(), SendCommandRequest(cid, app.sessionId, cmd, args, reply)), ::plainErrorHandler) { }
    }

    private fun leaveChannelImpl(cid: String) {
        val app = CommonApplication.getInstance(this)
        callService(app.findApiProvider().makePotatoApi().leaveChannel(app.findApiKey(), cid), ::plainErrorHandler) {
            val db = CommonApplication.getInstance(this).cacheDatabase
            db.deleteChannel(cid)
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_CHANNEL_LIST_UPDATED))
        }
    }

    private fun updateChannelVisibilityImpl(cid: String, visibility: Boolean) {
        val app = CommonApplication.getInstance(this)
        callService(app.findApiProvider().makePotatoApi().updateChannelVisibility(app.findApiKey(), cid, UpdateChannelVisibilityRequest(false)), ::plainErrorHandler) {
            val db = app.cacheDatabase
            db.updateVisibility(cid, !visibility)
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_CHANNEL_LIST_UPDATED))
        }
    }

    private fun createPublicChannelImpl(domainId: String, name: String, topic: String) {
        val app = CommonApplication.getInstance(this)
        val request = CreateChannelRequest.makePublicChannelRequest(domainId, name, if (topic == "") null else topic)
        callService(app.findApiProvider().makePotatoApi().createChannel(app.findApiKey(), request), ::plainErrorHandler) { channel ->
            val db = app.cacheDatabase
            DbTools.insertChannelIntoChannelsTable(db, channel.id, channel.domainId, channel.name, channel.unreadCount, channel.privateUserId, false)
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_CHANNEL_LIST_UPDATED))
        }
    }

    companion object {
        private const val ACTION_MARK_NOTIFICATIONS = "com.dhsdevelopments.potato.MARK_NOTIFICATIONS"
        private const val ACTION_LOAD_CHANNEL_LIST = "com.dhsdevelopments.potato.LOAD_CHANNELS"
        private const val ACTION_UPDATE_UNREAD_SUBSCRIPTION = "com.dhsdevelopments.potato.gcm.UPDATE_UNREAD_SUBSCRIPTION"
        private const val ACTION_SEND_MESSAGE_WITH_IMAGE = "com.dhsdevelopments.potato.gcm.SEND_MESSAGE_WITH_IMAGE"
        private const val ACTION_DELETE_MESSAGE = "com.dhsdevelopments.potato.DELETE_MESSAGE"
        private const val ACTION_SEND_COMMAND = "com.dhsdevelopments.potato.SEND_COMMAND"
        private const val ACTION_LEAVE_CHANNEL = "com.dhsdevelopments.potato.LEAVE_CHANNEL"
        private const val ACTION_UPDATE_CHANNEL_VISIBILITY = "com.dhsdevelopments.potato.UPDATE_CHANNEL_VISIBILITY"
        private const val ACTION_CREATE_PUBLIC_CHANNEL = "com.dhsdevelopments.potato.CREATE_PUBLIC_CHANNEL"

        private const val EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id"
        private const val EXTRA_UPDATE_STATE = "com.dhsdevelopments.potato.subscribe"
        private const val EXTRA_IMAGE_URI = "com.dhsdevelopments.potato.image"
        private const val EXTRA_MESSAGE_ID = "com.dhsdevelopments.potato.message_id"
        private const val EXTRA_CMD = "com.dhsdevelopments.potato.cmd"
        private const val EXTRA_ARGS = "com.dhsdevelopments.potato.args"
        private const val EXTRA_REPLY = "com.dhsdevelopments.potato.reply"
        private const val EXTRA_VISIBILITY = "com.dhsdevelopments.potato.visibility"
        private const val EXTRA_GROUP_ID = "com.dhsdevelopments.potato.group_id"
        private const val EXTRA_CHANNEL_NAME = "com.dhsdevelopments.potato.channel_name"
        private const val EXTRA_CHANNEL_TOPIC = "com.dhsdevelopments.potato.channel_topic"
        private const val EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id"

        const val ACTION_CHANNEL_LIST_UPDATED = "com.dhsdevelopments.potato.ACTION_CHANNEL_LIST_UPDATED"
        const val ACTION_CHANNEL_LIST_UPDATE_FAIL = "com.dhsdevelopments.potato.ACTION_CHANNEL_LIST_UPDATE_FAIL"
        const val EXTRA_ERROR_MESSAGE = "com.dhsdevelopments.potato.error_message"

        fun markNotificationsForChannel(context: Context, cid: String) {
            makeAndStartIntent(context, ACTION_MARK_NOTIFICATIONS,
                               EXTRA_CHANNEL_ID to cid)
        }

        fun loadChannelList(context: Context) {
            makeAndStartIntent(context, ACTION_LOAD_CHANNEL_LIST)
        }

        fun updateUnreadSubscriptionState(context: Context, cid: String, subscribe: Boolean) {
            makeAndStartIntent(context, ACTION_UPDATE_UNREAD_SUBSCRIPTION,
                               EXTRA_CHANNEL_ID to cid,
                               EXTRA_UPDATE_STATE to subscribe)
        }

        fun sendMessageWithImage(context: Context, cid: String, imageUri: Uri) {
            makeAndStartIntent(context, ACTION_SEND_MESSAGE_WITH_IMAGE,
                               EXTRA_CHANNEL_ID to cid,
                               EXTRA_IMAGE_URI to imageUri)
        }

        fun deleteMessage(context: Context, messageId: String) {
            makeAndStartIntent(context, ACTION_DELETE_MESSAGE,
                               EXTRA_MESSAGE_ID to messageId)
        }

        fun leaveChannel(context: Context, cid: String) {
            makeAndStartIntent(context, ACTION_LEAVE_CHANNEL,
                               EXTRA_CHANNEL_ID to cid)
        }

        fun updateChannelVisibility(context: Context, cid: String, visibility: Boolean) {
            makeAndStartIntent(context, ACTION_UPDATE_CHANNEL_VISIBILITY,
                               EXTRA_CHANNEL_ID to cid,
                               EXTRA_VISIBILITY to visibility)
        }

        fun sendCommand(context: Context, cid: String, cmd: String, args: String, reply: Boolean = false) {
            makeAndStartIntent(context, ACTION_SEND_COMMAND,
                               EXTRA_CHANNEL_ID to cid,
                               EXTRA_CMD to cmd,
                               EXTRA_ARGS to args,
                               EXTRA_REPLY to reply
            )
        }

        fun createPublicChannel(context: Context, domainId: String, name: String, topic: String) {
            makeAndStartIntent(context, ACTION_CREATE_PUBLIC_CHANNEL,
                               EXTRA_DOMAIN_ID to domainId,
                               EXTRA_CHANNEL_NAME to name,
                               EXTRA_CHANNEL_TOPIC to topic)
        }

        private fun makeAndStartIntent(context: Context, action: String, vararg extraElements: Pair<String, Any>) {
            val intent = Intent(context, RemoteRequestService::class.java)
            intent.action = action
            for ((key, value) in extraElements) {
                when (value) {
                    is String -> intent.putExtra(key, value)
                    is Boolean -> intent.putExtra(key, value)
                    is Parcelable -> intent.putExtra(key, value)
                    else -> throw IllegalArgumentException("Unexpected value type: " + value.javaClass.name)
                }
            }
            context.startService(intent)
        }
    }
}
