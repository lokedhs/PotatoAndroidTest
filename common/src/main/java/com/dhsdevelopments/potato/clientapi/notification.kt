@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.notifications

import com.dhsdevelopments.potato.clientapi.message.Message
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.reflect.Type

class NotificationTypeAdapter : JsonDeserializer<PotatoNotification> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PotatoNotification {
        val obj = json.asJsonObject
        val type = obj.get("type").asString
        return when (type) {
            "m" -> context.deserialize(obj, MessageNotification::class.java)
            "cu" -> context.deserialize(obj, StateUpdateNotification::class.java)
            "type" -> context.deserialize(obj, TypingNotification::class.java)
            "option" -> context.deserialize(obj, OptionNotification::class.java)
            "unknown-slashcommand" -> context.deserialize(obj, UnknownSlashcommandNotification::class.java)
            else -> context.deserialize(obj, DefaultNotification::class.java)
        }
    }
}

open class PotatoNotification : Serializable {
    @SerializedName("type")
    lateinit var type: String

    override fun toString() = "PotatoNotification[type='$type']"
}

class DefaultNotification : PotatoNotification() {
    override fun toString() = "DefaultNotification[type='$type']"
}

class MessageNotification : PotatoNotification() {
    @SerializedName("c")
    lateinit var message: Message
}

class StateUpdateNotification : PotatoNotification() {
    @SerializedName("add-type")
    lateinit var addType: String

    @SerializedName("user")
    var userStateUser: String? = null

    @SerializedName("users")
    var userStateSyncMembers: List<UserStateUpdateUser>? = null

    @SerializedName("channel")
    lateinit var channel: String

    override fun toString(): String {
        return "StateUpdateNotification[" +
                "addType='" + addType + '\'' +
                ", userStateUser='" + userStateUser + '\'' +
                ", userStateSyncMembers=" + userStateSyncMembers +
                ", channel='" + channel + '\'' +
                "] " + super.toString()
    }
}

class UserStateUpdateUser {
    @SerializedName("id")
    lateinit var id: String

    override fun toString() = "UserStateUpdateUser[id='$id']"
}

class PotatoNotificationResult {
    @SerializedName("event")
    var eventId: String? = null

    @SerializedName("data")
    var notifications: List<PotatoNotification>? = null

    override fun toString() = "PotatoNotificationResult[eventId='$eventId', notifications=$notifications]"
}

class TypingNotification : PotatoNotification() {
    @SerializedName("user")
    lateinit var userId: String

    @SerializedName("channel")
    lateinit var channelId: String

    @SerializedName("add-type")
    lateinit var addType: String

    override fun toString() = "TypingNotification[userId='$userId', channelId='$channelId', addType='$addType']"
}

class OptionNotification : PotatoNotification(), Serializable {
    @SerializedName("option-code")
    lateinit var optionCode: String

    @SerializedName("channel")
    lateinit var channel: String

    @SerializedName("title")
    lateinit var title: String

    @SerializedName("options")
    lateinit var options: List<Option>
}


class Option : Serializable {
    @SerializedName("title")
    lateinit var title: String

    @SerializedName("response")
    lateinit var response: String

    @SerializedName("image-url")
    var imageUrl: String? = null

    @SerializedName("button-text")
    var buttonText: String? = null
}

class UnknownSlashcommandNotification : PotatoNotification() {
    @SerializedName("channel")
    lateinit var channel: String

    @SerializedName("cmd")
    lateinit var cmd: String
}
