package com.dhsdevelopments.potato.userlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.channelmessages.HasChannelContentActivity
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult
import com.dhsdevelopments.potato.clientapi.users.User
import com.dhsdevelopments.potato.common.IntentUtil
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.service.ChannelSubscriptionService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

class ChannelUsersTracker private constructor(private val context: Context, val cid: String) {
    private val users = HashMap<String, UserDescriptor>()
    private val listeners = CopyOnWriteArraySet<UserActivityListener>()

    init {
        loadUsers()
    }

    fun getUsers(): Map<String, UserDescriptor> = users

    fun processIncoming(intent: Intent) {
        Log.d("processing channel user intent: $intent")
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
            users[uid] = UserDescriptor("noname", "unknown", null, active)
        }

        if (fireEvent) {
            listeners.forEach { it.userUpdated(uid) }
        }
    }

    private fun processSync(intent: Intent) {
        val uids = intent.getStringArrayExtra(ChannelSubscriptionService.EXTRA_CHANNEL_USERS_SYNC_USERS)
        Log.d("Got sync message. userList = ${Arrays.toString(uids)}")
        // Clear the active state of all current users
        users.values.forEach { it.isActive = false }
        uids.forEach { uid -> processAddRemove(uid, true, false) }
        fireUserListSync()
    }

    private fun updateUsers(members: List<User>) {
        for (u in members) {
            val d = users[u.id]
            if (d == null) {
                users[u.id] = UserDescriptor(u.description, u.nickname, u.imageName, false)
            }
            else {
                d.name = u.description
                d.nickname = u.nickname
            }
        }
        fireUserListSync()
    }

    private fun fireUserListSync() {
        listeners.forEach(UserActivityListener::activeUserListSync)
    }

    fun addUserActivityListener(listener: UserActivityListener) = listeners.add(listener)
    fun removeUserActivityListener(listener: UserActivityListener) = listeners.remove(listener)

    private fun loadUsers() {
        val app = PotatoApplication.getInstance(context)
        val call = app.findApiProvider().makePotatoApi().loadUsers(app.findApiKey(), cid)
        call.enqueue(object : Callback<LoadUsersResult> {
            override fun onResponse(call: Call<LoadUsersResult>, response: Response<LoadUsersResult>) {
                if (response.isSuccessful) {
                    updateUsers(response.body()!!.members)
                }
                else {
                    Log.e("Error code from server")
                    throw RuntimeException("Error code from server")
                }
            }

            override fun onFailure(call: Call<LoadUsersResult>, t: Throwable) {
                Log.e("Error loading users", t)
                throw RuntimeException("Error loading users", t)
            }
        })
    }

    fun getNameForUid(uid: String): String {
        val user = getUsers()[uid]
        return user?.name ?: "unknown"
    }

    fun getNicknameForUid(uid: String): String {
        val user = getUsers()[uid]
        return user?.nickname ?: "unknown"
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

        fun findForActivity(activity: Activity): ChannelUsersTracker {
            return (activity as HasChannelContentActivity).findUserTracker()
        }
    }

}
