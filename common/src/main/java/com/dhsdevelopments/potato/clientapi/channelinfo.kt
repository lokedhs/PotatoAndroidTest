@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.channelinfo

import com.dhsdevelopments.potato.clientapi.RemoteResult
import com.google.gson.annotations.SerializedName

/*
              (st-json:jso "id" (potato.core:channel/id channel)
                           "name" (potato.core:channel/name channel)
                           "topic" (potato.core:channel/topic channel)
                           "group" (potato.core:channel/group channel)
                           "group_type" (symbol-name (potato.core:group/type group))
                           "unread_count" (or (getfield :|count| u) 0)
                           "domain" (potato.core:channel/domain channel)
                           "private_user" (if (eq (potato.core:group/type group) :private)
                                              (potato.private:find-chat-counterpart channel-users (potato.core:current-user))
                                              :null)))))))
*/

class LoadChannelInfoResult : RemoteResult {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("name")
    lateinit var name: String

    @SerializedName("topic")
    lateinit var topic: String

    @SerializedName("group")
    lateinit var groupId: String

    @SerializedName("group_type")
    lateinit var groupType: String

    @SerializedName("unread_count")
    var unreadCount: Int = 0

    @SerializedName("domain")
    lateinit var domainId: String

    @SerializedName("private_user")
    var privateUserId: String? = null

    override fun errorMsg(): String? = null
}

class CreateChannelRequest(
        @SerializedName("domain")
        val domain: String?,

        @SerializedName("group")
        val group: String?,

        @SerializedName("name")
        val name: String,

        @SerializedName("topic")
        val topic: String?

) {
    companion object {
        fun makePublicChannelRequest(domainId: String, name: String, topic: String? = null) = CreateChannelRequest(domainId, null, name, topic)
        fun makeChannelRequest(groupId: String, name: String, topic: String? = null) = CreateChannelRequest(null, groupId, name, topic)
    }
}
