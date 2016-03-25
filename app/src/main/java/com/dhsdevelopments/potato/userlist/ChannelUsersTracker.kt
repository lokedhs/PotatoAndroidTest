package com.dhsdevelopments.potato.userlist

import android.content.Context
import android.content.Intent
import com.dhsdevelopments.potato.IntentUtil
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.clientapi.LoadUsersResult
import com.dhsdevelopments.potato.clientapi.User
import com.dhsdevelopments.potato.service.ChannelSubscriptionService
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class ChannelUsersTracker private constructor(private val context: Context, val cid: String) {
    private val users = HashMap<String, UserDescriptor>()
    private val listeners = CopyOnWriteArraySet<UserActivityListener>()

    init {
        loadUsers()
    }

    fun getUsers(): Map<String, UserDescriptor> {
        return users
    }

    fun processIncoming(intent: Intent) {
        Log.d("processing channel user intent: " + intent)
        if (intent.action != ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE) {
            // We only want to process channel users notifications
            return
        }

        if (intent.getStringExtra(IntentUtil.EXTRA_CHANNEL_ID) != cid) {
            // Only accept notifications for the given channel
            return
        }

        when (intent.getStringExtra(ChannelSubscriptionService.EXTRA_CHANNEL_USERS_TYPE)) {
            ChannelSubscriptionService.USER_UPDATE_TYPE_SYNC -> processSync(intent)
            ChannelSubscriptionService.USER_UPDATE_TYPE_ADD -> processAddRemove(intent.getStringExtra(ChannelSubscriptionService.EXTRA_CHANNEL_USERS_USER_ID), true, true)
            ChannelSubscriptionService.USER_UPDATE_TYPE_REMOVE -> processAddRemove(intent.getStringExtra(ChannelSubscriptionService.EXTRA_CHANNEL_USERS_USER_ID), false, true)
        }
    }

    private fun processAddRemove(uid: String, active: Boolean, fireEvent: Boolean) {
        val d = users[uid]
        if (d != null) {
            d.isActive = active
        }
        else {
            users.put(uid, UserDescriptor("noname", "unknown", null, active))
        }

        if (fireEvent) {
            for (l in listeners) {
                l.userUpdated(uid)
            }
        }
    }

    private fun processSync(intent: Intent) {
        val uids = intent.getStringArrayExtra(ChannelSubscriptionService.EXTRA_CHANNEL_USERS_SYNC_USERS)
        Log.d("Got sync message. userList = " + Arrays.toString(uids))
        // Clear the activate state of all current users
        for (d in users.values) {
            d.isActive = false
        }
        for (uid in uids) {
            processAddRemove(uid, true, false)
        }
        fireUserListSync()
    }

    private fun updateUsers(members: List<User>) {
        for (u in members) {
            val d = users[u.id]
            if (d == null) {
                users.put(u.id, UserDescriptor(u.description, u.nickname, u.imageName, false))
            }
            else {
                d.name = u.description
                d.nickname = u.nickname
            }
        }
        fireUserListSync()
    }

    private fun fireUserListSync() {
        for (l in listeners) {
            l.activeUserListSync()
        }
    }

    fun addUserActivityListener(listener: UserActivityListener) {
        listeners.add(listener)
    }

    fun removeUserActivityListener(listener: UserActivityListener) {
        listeners.remove(listener)
    }

    fun loadUsers() {
        val app = PotatoApplication.getInstance(context)
        val call = app.potatoApi.loadUsers(app.apiKey, cid)
        call.enqueue(object : Callback<LoadUsersResult> {
            override fun onResponse(response: Response<LoadUsersResult>, retrofit: Retrofit) {
                if (response.isSuccess) {
                    updateUsers(response.body().members)
                }
                else {
                    Log.wtf("Error code from server")
                }
            }

            override fun onFailure(t: Throwable) {
                Log.wtf("Error loading users", t)
            }
        })
    }

    fun getNameForUid(uid: String): String {
        val user = getUsers()[uid]
        if (user == null) {
            return "unknown"
        }
        else {
            return user.name
        }
    }

    fun getNicknameForUid(uid: String): String {
        val user = getUsers()[uid]
        if (user == null) {
            return "unknown"
        }
        else {
            return user.nickname
        }
    }

    inner class UserDescriptor(
            var name: String,
            var nickname: String,
            var imageName: String?,
            var isActive: Boolean)

    interface UserActivityListener {
        fun activeUserListSync()

        fun userUpdated(uid: String)
    }

    companion object {
        fun findForChannel(context: Context, cid: String): ChannelUsersTracker {
            return ChannelUsersTracker(context, cid)
        }
    }
}
