package com.dhsdevelopments.potato.clientapi

import com.dhsdevelopments.potato.clientapi.notifications.MessageNotification
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification
import com.dhsdevelopments.potato.clientapi.notifications.StateUpdateNotification
import com.dhsdevelopments.potato.clientapi.notifications.TypingNotification
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class NotificationTypeAdapter : JsonDeserializer<PotatoNotification> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PotatoNotification {
        val obj = json.asJsonObject
        val type = obj.get("type").asString
        when (type) {
            "m" -> return context.deserialize<PotatoNotification>(obj, MessageNotification::class.java)
            "cu" -> return context.deserialize<PotatoNotification>(obj, StateUpdateNotification::class.java)
            "type" -> return context.deserialize<PotatoNotification>(obj, TypingNotification::class.java)
            else -> return context.deserialize<PotatoNotification>(obj, PotatoNotification::class.java)
        }
    }
}
