package com.dhsdevelopments.potato.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.clientapi.ChannelUpdatesUpdateResult
import com.dhsdevelopments.potato.clientapi.PotatoApi
import com.dhsdevelopments.potato.clientapi.notifications.*
import retrofit.Call
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.io.IOException
import java.io.InterruptedIOException
import java.util.*

class ChannelSubscriptionService : Service() {

    private var receiverThread: Receiver? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        when (action) {
            ACTION_BIND_TO_CHANNEL -> bindToChannel(intent.getStringExtra(EXTRA_CHANNEL_ID))
            ACTION_UNBIND_FROM_CHANNEL -> unbindFromChannel(intent.getStringExtra(EXTRA_CHANNEL_ID))
            else -> throw UnsupportedOperationException("Illegal subscription command: " + action)
        }
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.i("Destroying subscription service")
        if (receiverThread != null) {
            receiverThread!!.requestShutdown()
        }
        super.onDestroy()
    }

    private fun unbindFromChannel(cid: String) {
        if (receiverThread == null) {
            Log.w("Attempt to unbind with no thread running")
        }
        else {
            val wasShutdown = receiverThread!!.unbindFromChannel(cid)
            if (wasShutdown) {
                receiverThread = null
            }
        }
    }

    private fun bindToChannel(cid: String) {
        Log.i("Binding to channel: " + cid)
        if (receiverThread == null) {
            receiverThread = Receiver(cid)
            receiverThread!!.start()
        }
        else {
            receiverThread!!.bindToChannel(cid)
        }
    }

    private fun processNewNotifications(notifications: List<PotatoNotification>) {
        for (n in notifications) {
            Log.i("Processing notification: " + n)
            if (n is MessageNotification) {
                processMessageNotification(n)
            }
            else if (n is StateUpdateNotification) {
                processStateUpdateNotification(n)
            }
            else if (n is TypingNotification) {
                processTypingNotification(n)
            }
        }
    }

    private fun processMessageNotification(notification: MessageNotification) {
        val msg = notification.message
        val intent = Intent(ACTION_MESSAGE_RECEIVED)
        intent.putExtra(EXTRA_MESSAGE, msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun processStateUpdateNotification(update: StateUpdateNotification) {
        val intent = Intent(ACTION_CHANNEL_USERS_UPDATE)
        intent.putExtra(EXTRA_CHANNEL_ID, update.channel)
        when (update.addType) {
            "sync" -> {
                intent.putExtra(EXTRA_CHANNEL_USERS_TYPE, USER_UPDATE_TYPE_SYNC)
                intent.putExtra(EXTRA_CHANNEL_USERS_SYNC_USERS, userListToUserIdArray(update.userStateSyncMembers!!))
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            "add" -> {
                intent.putExtra(EXTRA_CHANNEL_USERS_TYPE, USER_UPDATE_TYPE_ADD)
                intent.putExtra(EXTRA_CHANNEL_USERS_USER_ID, update.userStateUser)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            "remove" -> {
                intent.putExtra(EXTRA_CHANNEL_USERS_TYPE, USER_UPDATE_TYPE_REMOVE)
                intent.putExtra(EXTRA_CHANNEL_USERS_USER_ID, update.userStateUser)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
            else -> Log.w("Unexpected addType: " + update.addType)
        }
    }

    private fun processTypingNotification(typingNotification: TypingNotification) {
        val intent = Intent(ACTION_TYPING)
        intent.putExtra(EXTRA_CHANNEL_ID, typingNotification.channelId)
        intent.putExtra(EXTRA_USER_ID, typingNotification.userId)
        intent.putExtra(EXTRA_TYPING_MODE, typingModeFromJson(typingNotification.addType))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun typingModeFromJson(addType: String): String {
        when (addType) {
            "begin" -> return TYPING_MODE_ADD
            "end" -> return TYPING_MODE_REMOVE
            else -> throw IllegalStateException("Unexpected typing mode from server: \"" + addType + "\"")
        }
    }

    private fun userListToUserIdArray(userStateSyncMembers: List<UserStateUpdateUser>): Array<String?> {
        val result = arrayOfNulls<String>(userStateSyncMembers.size)
        var i = 0
        for (u in userStateSyncMembers) {
            result[i++] = u.id
        }
        return result
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private inner class Receiver(private val cid: String) : Thread("NotificationReceiver") {
        private val api: PotatoApi
        private val apiKey: String

        private var isShutdown = false
        private val subscribedChannels = HashSet<String>()
        private var pendingBinds: MutableSet<String>? = HashSet()
        private var eventId: String? = null
        private var outstandingCall: Call<PotatoNotificationResult>? = null

        init {
            val app = PotatoApplication.getInstance(this@ChannelSubscriptionService)

            api = app.potatoApiLongTimeout
            apiKey = app.apiKey

            subscribedChannels.add(cid)
        }

        override fun run() {
            val handler = Handler(this@ChannelSubscriptionService.mainLooper)

            try {
                while (!isShutdown && !Thread.interrupted()) {
                    val call = api.channelUpdates(apiKey, cid, "content,state", eventId)
                    synchronized (this) {
                        outstandingCall = call
                    }
                    try {
                        val response = call.execute()

                        synchronized (this) {
                            outstandingCall = null
                        }

                        if (response.isSuccess) {
                            val body = response.body()

                            updateEventIdAndCheckPendingBindRequests(body.eventId)

                            val notifications = body.notifications
                            if (notifications != null && !notifications.isEmpty()) {
                                handler.post { processNewNotifications(notifications) }
                            }
                        }
                        else {
                            Log.e("Error reading notifications: " + response.message())
                            Thread.sleep(10000)
                        }
                    }
                    catch (e: InterruptedIOException) {
                        throw ReceiverStoppedException(e)
                    }
                    catch (e: IOException) {
                        // If an error occurs, wait for a while before trying again
                        if (!isShutdown) {
                            Log.e("Got exception when waiting for updates", e)
                            Thread.sleep(10000)
                        }
                    }

                }
            }
            catch (e: InterruptedException) {
                if (!isShutdown) {
                    Log.wtf("Got interruption while not being shutdown", e)
                }
            }
            catch (e: ReceiverStoppedException) {
                if (!isShutdown) {
                    Log.wtf("Receiver stop requested while not in shutdown state", e)
                }
            }

            Log.i("Updates thread shut down")
        }

        private fun updateEventIdAndCheckPendingBindRequests(eventId: String?) {
            if (eventId == null) {
                throw IllegalStateException("Received eventId was null when updating")
            }

            var pendingBindsCopy: Set<String>? = null
            synchronized (this) {
                this.eventId = eventId
                if (isShutdown) {
                    return
                }
                if (!pendingBinds!!.isEmpty()) {
                    pendingBindsCopy = pendingBinds
                    pendingBinds = null
                }
            }
            if (pendingBindsCopy != null) {
                for (s in pendingBindsCopy!!) {
                    submitBindRequest(s)
                }
            }
        }


        fun bindToChannel(cid: String) {
            var willAdd = false
            synchronized (this) {
                if (isShutdown) {
                    Log.w("Not binding since the connection is being shut down")
                    return
                }
                if (!subscribedChannels.contains(cid)) {
                    subscribedChannels.add(cid)
                    if (eventId == null) {
                        pendingBinds!!.add(cid)
                    }
                    else {
                        willAdd = true
                    }
                }
            }
            if (willAdd) {
                submitBindRequest(cid)
            }
        }

        private fun submitBindRequest(cid: String) {
            Log.i("Submit bind request: " + cid)
            if (eventId == null) {
                throw IllegalStateException("eventId is null")
            }
            val call = api.channelUpdatesUpdate(apiKey, eventId, "add", cid, "content,state")
            call.enqueue(object : Callback<ChannelUpdatesUpdateResult> {
                override fun onResponse(response: Response<ChannelUpdatesUpdateResult>, retrofit: Retrofit) {
                    if (response.isSuccess) {
                        if ("ok" != response.body().result) {
                            Log.wtf("Unexpected result form bind call")
                        }
                    }
                    else {
                        Log.wtf("Got failure from server: " + response.message())
                    }
                }

                override fun onFailure(t: Throwable) {
                    Log.wtf("Failed to bind", t)
                }
            })
        }

        fun unbindFromChannel(cid: String): Boolean {
            var wasShutdown = false
            synchronized (this) {
                subscribedChannels.remove(cid)
                pendingBinds!!.remove(cid)
                if (subscribedChannels.isEmpty()) {
                    requestShutdown()
                    wasShutdown = true
                }
            }
            // TODO: Send unbind request to server here
            return wasShutdown
        }

        internal fun requestShutdown() {
            val outstandingCallCopy = synchronized (this) {
                isShutdown = true
                outstandingCall
            }
            outstandingCallCopy?.cancel()

            interrupt()
        }
    }

    companion object {
        val ACTION_BIND_TO_CHANNEL = "com.dhsdevelopments.potato.BIND_CHANNEL"
        val ACTION_UNBIND_FROM_CHANNEL = "com.dhsdevelopments.potato.UNBIND_CHANNEL"
        val EXTRA_CHANNEL_ID = "com.dhsdevelopments.potato.channel_id"

        val ACTION_MESSAGE_RECEIVED = "com.dhsdevelopments.potato.MESSAGE_RECEIVED"
        val EXTRA_MESSAGE = "com.dhsdevelopments.potato.message"

        val ACTION_CHANNEL_USERS_UPDATE = "com.dhsdevelopments.potato.CHANNEL_USERS_UPDATED"
        val EXTRA_CHANNEL_USERS_SYNC_USERS = "com.dhsdevelopments.potato.sync_users"
        val EXTRA_CHANNEL_USERS_USER_ID = "com.dhsdevelopments.potato.update_user"
        val EXTRA_CHANNEL_USERS_TYPE = "com.dhsdevelopments.potato.update_user_add_type"
        val USER_UPDATE_TYPE_SYNC = "sync"
        val USER_UPDATE_TYPE_ADD = "add"
        val USER_UPDATE_TYPE_REMOVE = "remove"

        val ACTION_TYPING = "com.dhsdevelopments.potato.TYPING_UPDATED"
        val EXTRA_TYPING_MODE = "com.dhsdevelopments.potato.typing_mode"
        val EXTRA_USER_ID = "com.dhsdevelopments.potato.user_id"
        val TYPING_MODE_ADD = "add"
        val TYPING_MODE_REMOVE = "remove"
    }
}
