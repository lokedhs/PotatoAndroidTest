package com.dhsdevelopments.potato.clientapi

import com.dhsdevelopments.potato.clientapi.message.*
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.util.*

class MessageElementTypeAdapter : JsonDeserializer<MessageElement> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MessageElement {
        if (json.isJsonPrimitive) {
            return MessageElementString(json.asString)
        }
        else if (json.isJsonArray) {
            val a = json.asJsonArray
            val result = ArrayList<MessageElement>(a.size())
            for (v in a) {
                val element = context.deserialize<MessageElement>(v, MessageElement::class.java)
                result.add(element)
            }
            return MessageElementList(result)
        }
        else {
            val obj = json.asJsonObject

            val type = obj.get("type").asString
            return when (type) {
                "p" -> {
                    val element = obj.get("e")
                    val messageElement = context.deserialize<MessageElement>(element, MessageElement::class.java)
                    MessageElementParagraph(messageElement)
                }
                "b" -> {
                    val element = obj.get("e")
                    val messageElement = context.deserialize<MessageElement>(element, MessageElement::class.java)
                    MessageElementBold(messageElement)
                }
                "i" -> {
                    val element = obj.get("e")
                    val messageElement = context.deserialize<MessageElement>(element, MessageElement::class.java)
                    MessageElementItalics(messageElement)
                }
                "code" -> {
                    val element = obj.get("e")
                    val messageElement = context.deserialize<MessageElement>(element, MessageElement::class.java)
                    MessageElementCode(messageElement)
                }
                "url" -> {
                    val addr = obj.get("addr").asString
                    val description = obj.get("description")
                    MessageElementUrl(addr, if (description.isJsonNull) addr else description.asString)
                }
                "code-block" -> {
                    val language = obj.get("language").asString
                    val code = obj.get("code").asString
                    MessageElementCodeBlock(language, code)
                }
                "user" -> {
                    val userId = obj.get("user_id").asString
                    val userDescription = obj.get("user_description").asString
                    MessageElementUser(userId, userDescription)
                }
                "newline" -> return MessageElementNewline()
                else -> MessageElementUnknownType(type)
            }
        }
    }
}
