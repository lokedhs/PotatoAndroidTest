package com.dhsdevelopments.potato.service

import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.dhsdevelopments.potato.IntentUtil
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.clientapi.ChannelUpdatesUpdateResult
import com.dhsdevelopments.potato.clientapi.PotatoApi
import com.dhsdevelopments.potato.clientapi.notifications.*
import com.dhsdevelopments.potato.isChannelJoined
import retrofit.Call
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.io.IOException
import java.io.InterruptedIOException
import java.util.*

class ChannelSubscriptionService : Service() {

    private var receiverThread: Receiver? = null
    private var lastStartId: Int? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        lastStartId = startId
        val action = intent.action
        when (action) {
            ACTION_BIND_TO_CHANNEL -> bindToChannel(intent.getStringExtra(IntentUtil.EXTRA_CHANNEL_ID))
            ACTION_UNBIND_FROM_CHANNEL -> unbindFromChannel(intent.getStringExtra(IntentUtil.EXTRA_CHANNEL_ID))
            else -> throw UnsupportedOperationException("Illegal subscription command: " + action)
        }
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        if (receiverThread != null) {
            receiverThread!!.requestShutdown()
        }
        Log.d("Receiver service destroyed")
        super.onDestroy()
    }

    private fun unbindFromChannel(cid: String) {
        Log.d("unbinding from $cid, hasThread=${receiverThread != null}")
        if (receiverThread == null) {
            IllegalStateException("Attempt to unbind with no thread running")
        }
        else {
            val wasShutdown = receiverThread!!.unbindFromChannel(cid)
            if (wasShutdown) {
                receiverThread = null
            }
        }
    }

    private fun bindToChannel(cid: String) {
        Log.d("Binding to channel: " + cid)
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
            Log.d("Processing notification: " + n)
            when (n) {
                is MessageNotification -> processMessageNotification(n)
                is StateUpdateNotification -> processStateUpdateNotification(n)
                is TypingNotification -> processTypingNotification(n)
                is OptionNotification -> processOptionNotification(n)
                is UnknownSlashcommandNotification -> processUnknownSlashcommandNotification(n)
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
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, update.channel)
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
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, typingNotification.channelId)
        intent.putExtra(IntentUtil.EXTRA_USER_ID, typingNotification.userId)
        intent.putExtra(EXTRA_TYPING_MODE, typingModeFromJson(typingNotification.addType))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun typingModeFromJson(addType: String): String {
        return when (addType) {
            "begin" -> TYPING_MODE_ADD
            "end" -> TYPING_MODE_REMOVE
            else -> throw IllegalStateException("Unexpected typing mode from server: \"$addType\"")
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

    private fun processOptionNotification(notification: OptionNotification) {
        Log.d("Got option notification")
        val intent = Intent(ACTION_OPTIONS)
        intent.putExtra(EXTRA_OPTION_NOTIFICATION, notification)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun processUnknownSlashcommandNotification(n: UnknownSlashcommandNotification) {
        Log.d("Unknown slashcommand: ${n.cmd}, channel: ${n.channel}")
        val intent = Intent(ACTION_UNKNOWN_SLASHCOMMAND_RESPONSE)
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, n.channel)
        intent.putExtra(EXTRA_COMMAND_NAME, n.cmd)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun updateChannelDatabaseIfNeeded(cid: String) {
        if(!isChannelJoined(this, cid)) {
            RemoteRequestService.loadChannelList(this)
        }
    }

    private inner class Receiver(private val cid: String) : Thread("NotificationReceiver") {
        private val api: PotatoApi
        private val apiKey: String
        private val sid: String

        private var isShutdown = false
        private val subscribedChannels = HashSet<String>()
        private var pendingBinds: MutableSet<String>? = null
        private var eventId: String? = null
        private var outstandingCall: Call<PotatoNotificationResult>? = null

        init {
            val app = PotatoApplication.getInstance(this@ChannelSubscriptionService)

            api = app.potatoApiLongTimeout
            apiKey = app.apiKey
            sid = app.sessionId

            subscribedChannels.add(cid)
        }

        override fun run() {
            val handler = Handler(this@ChannelSubscriptionService.mainLooper)

            try {
                while (!isShutdown && !Thread.interrupted()) {
                    val call = api.channelUpdates(apiKey, cid, "content,state,session", eventId, sid)
                    synchronized (this) {
                        outstandingCall = call
                    }
                    try {
                        val response = call.execute()

                        synchronized (this) {
                            outstandingCall = null
                        }

                        if (response.isSuccess) {
                            // It seems as though response.body() is null when the outstanding call has been cancelled.
                            val body = response.body() ?: throw ReceiverStoppedException()
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
                        Log.d("Subscriber interrupted")
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

            Log.d("Updates thread shut down")

            if(lastStartId != null) {
                stopSelf(lastStartId!!)
            }
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
                if (pendingBinds != null && !pendingBinds!!.isEmpty()) {
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
                        if(pendingBinds == null) {
                            pendingBinds = HashSet()
                        }
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
            Log.d("Submit bind request: " + cid)
            val e = eventId ?: throw IllegalStateException("eventId is null")
            val call = api.channelUpdatesUpdate(apiKey, e, "add", cid, "content,state")
            call.enqueue(object : Callback<ChannelUpdatesUpdateResult> {
                override fun onResponse(response: Response<ChannelUpdatesUpdateResult>, retrofit: Retrofit) {
                    if (response.isSuccess) {
                        if ("ok" != response.body().result) {
                            Log.wtf("Unexpected result form bind call")
                        }
                        else {
                            updateChannelDatabaseIfNeeded(cid)
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
                if(pendingBinds != null) {
                    pendingBinds!!.remove(cid)
                }
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
                val call = outstandingCall
                outstandingCall = null
                call
            }
            if(outstandingCallCopy != null) {
                val task = object : AsyncTask<Unit, Unit, Unit>() {
                    override fun doInBackground(vararg params: Unit?) {
                        outstandingCallCopy.cancel()
                    }
                }
                task.execute()
            }

            interrupt()
        }


        private inner class ReceiverStoppedException : Exception {
            constructor()
            constructor(e: InterruptedIOException) : super(e)
        }
    }

    companion object {
        val ACTION_BIND_TO_CHANNEL = "com.dhsdevelopments.potato.BIND_CHANNEL"
        val ACTION_UNBIND_FROM_CHANNEL = "com.dhsdevelopments.potato.UNBIND_CHANNEL"

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
        val TYPING_MODE_ADD = "add"
        val TYPING_MODE_REMOVE = "remove"

        val ACTION_UNKNOWN_SLASHCOMMAND_RESPONSE = "com.dhsdevelopments.potato.UNKNOWN_COMMAND"
        val EXTRA_COMMAND_NAME = "command_name"

        val ACTION_OPTIONS = "com.dhsdevelopments.potato.SESSION_OPTIONS_REQUEST"
        val EXTRA_OPTION_NOTIFICATION = "options"
    }
}
