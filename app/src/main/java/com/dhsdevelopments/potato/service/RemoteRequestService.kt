package com.dhsdevelopments.potato.service

import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Parcelable
import android.support.v4.content.LocalBroadcastManager
import android.webkit.MimeTypeMap
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.StorageHelper
import com.dhsdevelopments.potato.clientapi.ClearNotificationsResult
import com.dhsdevelopments.potato.clientapi.ImageUriRequestBody
import com.dhsdevelopments.potato.clientapi.channel2.Channel
import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult
import com.dhsdevelopments.potato.clientapi.channel2.Domain
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationRequest
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationResult
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import retrofit.Call
import retrofit.Response

import java.io.IOException
import java.util.HashMap

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class RemoteRequestService : IntentService("RemoteRequestService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                ACTION_MARK_NOTIFICATIONS -> markNotificationsForChannelImpl(intent.getStringExtra(EXTRA_CHANNEL_ID))
                ACTION_LOAD_CHANNEL_LIST -> loadChannelListImpl()
                ACTION_UPDATE_UNREAD_SUBSCRIPTION -> updateUnreadSubscriptionStateImpl(intent.getStringExtra(EXTRA_CHANNEL_ID), intent.getBooleanExtra(EXTRA_UPDATE_STATE, false))
                ACTION_SEND_MESSAGE_WITH_IMAGE -> sendMessageWithImageImpl(intent.getStringExtra(EXTRA_CHANNEL_ID), intent.getParcelableExtra<Parcelable>(EXTRA_IMAGE_URI) as Uri)
            }
        }
    }

    private fun sendMessageWithImageImpl(cid: String, imageUri: Uri) {
        val app = PotatoApplication.getInstance(this)
        val map = HashMap<String, Any>()

        map.put("content", SendMessageRequest("Uploaded file from mobile"))

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(imageUri))
        map.put("body\"; filename=\"file." + (extension ?: "jpg") + "\" ", ImageUriRequestBody(this, imageUri))

        Log.i("extension=" + extension + ", type map: " + map.keys)

        val call = app.potatoApi.sendMessageWithFile(app.apiKey, cid, map)
        try {
            val response = call.execute()
            if (response.isSuccess) {
                if ("ok" == response.body().result) {
                    Log.i("Uploaded image, messageId=" + response.body().id)
                } else {
                    Log.e("Got error code from server: " + response.body().result)
                }
            } else {
                Log.e("HTTP error when uploading image. code=" + response.code() + ", message=" + response.message())
            }
        } catch (e: IOException) {
            // TODO: We really need a good generic way of handling IO errors
            Log.e("Error when uploading image", e)
        }

    }

    private fun markNotificationsForChannelImpl(cid: String) {
        try {
            val app = PotatoApplication.getInstance(this)
            val call = app.potatoApi.clearNotificationsForChannel(app.apiKey, cid)
            val result = call.execute()
            if (result.isSuccess) {
                if ("ok" == result.body().result) {
                    Log.d("Notifications cleared for channel: " + cid)
                } else {
                    Log.e("Unexpected result from notification clear request: " + result.body())
                }
            } else {
                Log.e("Unable to clear notifications on server: code=" + result.code() + ", message=" + result.message())
            }
        } catch (e: IOException) {
            Log.e("Exception when clearing notifications", e)
        }

    }

    private fun loadChannelListImpl() {
        try {
            val app = PotatoApplication.getInstance(this)
            val call = app.potatoApi.getChannels2(app.apiKey)
            val result = call.execute()

            if (result.isSuccess) {
                val db = app.cacheDatabase
                db.beginTransaction()
                try {
                    // We need to delete everything from the table since this call returns the full state
                    db.delete(StorageHelper.CHANNELS_TABLE, null, null)
                    db.delete(StorageHelper.DOMAINS_TABLE, null, null)

                    for (d in result.body().domains) {
                        if (d.type != "PRIVATE") {
                            val values = ContentValues()
                            values.put(StorageHelper.DOMAINS_ID, d.id)
                            values.put(StorageHelper.DOMAINS_NAME, d.name)
                            db.insert(StorageHelper.DOMAINS_TABLE, null, values)

                            for (c in d.channels) {
                                val channelValues = ContentValues()
                                channelValues.put(StorageHelper.CHANNELS_ID, c.id)
                                channelValues.put(StorageHelper.CHANNELS_DOMAIN, d.id)
                                channelValues.put(StorageHelper.CHANNELS_NAME, c.name)
                                channelValues.put(StorageHelper.CHANNELS_UNREAD, c.unreadCount)
                                channelValues.put(StorageHelper.CHANNELS_PRIVATE, c.privateUser)
                                db.insert(StorageHelper.CHANNELS_TABLE, null, channelValues)
                            }
                        }
                    }

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }

                val mgr = LocalBroadcastManager.getInstance(this)
                val intent = Intent(ACTION_CHANNEL_LIST_UPDATED)
                mgr.sendBroadcast(intent)
            }
        } catch (e: IOException) {
            Log.e("Exception when loading channels", e)
        }

    }

    private fun updateUnreadSubscriptionStateImpl(cid: String, add: Boolean) {
        try {
            val app = PotatoApplication.getInstance(this)
            val token = InstanceID.getInstance(this).getToken(getString(R.string.gcm_sender_id), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
            val call = app.potatoApi.updateUnreadNotification(app.apiKey, cid, UpdateUnreadNotificationRequest(token, add))
            val result = call.execute()
            if (result.isSuccess) {
                if ("ok" == result.body().result) {
                    Log.d("Subscription updated successfully")
                } else {
                    Log.e("Unexpected reply from unread subscription call")
                }
            } else {
                Log.e("Got error from server when subscribing to unread: " + result.message())
            }
        } catch (e: IOException) {
            Log.e("Exception while updating subscription state")
        }
    }

    companion object {
        private val ACTION_MARK_NOTIFICATIONS = "com.dhsdevelopments.potato.MARK_NOTIFICATIONS"
        private val ACTION_LOAD_CHANNEL_LIST = "com.dhsdevelopments.potato.LOAD_CHANNELS"
        private val ACTION_UPDATE_UNREAD_SUBSCRIPTION = "com.dhsdevelopments.potato.gcm.UPDATE_UNREAD_SUBSCRIPTION"
        private val ACTION_SEND_MESSAGE_WITH_IMAGE = "com.dhsdevelopments.potato.gcm.SEND_MESSAGE_WITH_IMAGE"
        private val EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id"
        private val EXTRA_UPDATE_STATE = "com.dhsdevelopments.potato.subscribe"
        private val EXTRA_IMAGE_URI = "com.dhsdevelopments.potato.image"

        @JvmField val ACTION_CHANNEL_LIST_UPDATED = "com.dhsdevelopments.potato.ACTION_CHANNEL_LIST_UPDATED"

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

        private fun makeAndStartIntent(context: Context, action: String, vararg extraElements: Pair<String,Any>) {
            val intent = Intent(context, RemoteRequestService::class.java)
            intent.setAction(action)
            for(v in extraElements) {
                val key = v.first
                val value = v.second
                if (value is String) {
                    intent.putExtra(key, value)
                } else if (value is Boolean) {
                    intent.putExtra(key, value)
                } else if (value is Parcelable) {
                    intent.putExtra(key, value)
                } else {
                    throw IllegalArgumentException("Unexpected value type: " + value.javaClass.getName())
                }
            }
            context.startService(intent)
        }
    }
}
