package com.dhsdevelopments.potato.clientapi.message

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import com.dhsdevelopments.potato.CodeBlockBackgroundSpan
import com.dhsdevelopments.potato.CodeBlockTypefaceSpan
import com.dhsdevelopments.potato.CodeTypefaceSpan
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.Serializable
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
            fun makeElement(): MessageElement {
                return context.deserialize<MessageElement>(obj.get("e"), MessageElement::class.java)
            }

            val type = obj.get("type").asString
            return when (type) {
                "p" -> MessageElementParagraph(makeElement())
                "b" -> MessageElementBold(makeElement())
                "i" -> MessageElementItalics(makeElement())
                "code" -> MessageElementCode(makeElement())
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
                "newline" -> MessageElementNewline()
                else -> MessageElementUnknownType(type)
            }
        }
    }
}

class MessageElementList(private val list: List<MessageElement>) : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val builder = SpannableStringBuilder()
        for (element in list) {
            builder.append(element.makeSpan(context))
        }
        return builder
    }

    override fun toString(): String {
        return "MessageElementList[" +
                "list=" + list +
                "] " + super.toString()
    }
}

class Message : Serializable {
    @SerializedName("id")
    lateinit var id: String

    @SerializedName("channel")
    lateinit var channel: String

    @SerializedName("created_date")
    lateinit var createdDate: String

    @SerializedName("from")
    lateinit var from: String

    @SerializedName("from_name")
    lateinit var fromName: String

    @SerializedName("text")
    lateinit var text: MessageElement

    @SerializedName("use_math")
    var useMath: Boolean = false

    @SerializedName("deleted")
    var deleted: Boolean = false

    @SerializedName("hash")
    lateinit var hash: String

    @SerializedName("updated")
    var updated: Int? = null

    @SerializedName("updated_date")
    var updatedDate: String? = null

    @SerializedName("extra_html")
    var extraHtml: String? = null

    @SerializedName("star_users")
    lateinit var starUsers: List<String>

    @SerializedName("image")
    var messageImage: MessageImage? = null

    override fun toString(): String {
        return "Message[id='$id', channel='$channel', createdDate='$createdDate', from='$from', fromName='$fromName', text=$text, useMath=$useMath, deleted=$deleted, hash='$hash', updated=$updated, extraHtml='$extraHtml', starUsers=$starUsers]"
    }
}

class MessageElementCode(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val s = SpannableString(content.makeSpan(context))
        s.setSpan(CodeTypefaceSpan(), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}

class MessageElementCodeBlock(private val language: String, private val code: String) : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val adjustedString = "\n" + code + "\n"
        val s = SpannableString(adjustedString)
        s.setSpan(CodeBlockTypefaceSpan(), 1, adjustedString.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(CodeBlockBackgroundSpan(), 1, adjustedString.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }

    override fun toString(): String {
        return "MessageElementCodeBlock[" +
                "language='" + language + '\'' +
                ", code='" + code + '\'' +
                "] " + super.toString()
    }
}

class MessageElementNewline : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        return "\n"
    }
}

class MessageElementItalics(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val s = SpannableString(content.makeSpan(context))
        s.setSpan(StyleSpan(Typeface.ITALIC), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}

class MessageImage : Serializable {
    @SerializedName("file")
    lateinit var file: String

    @SerializedName("width")
    var width: Int = 0

    @SerializedName("height")
    var height: Int = 0
}

class MessageElementString(private val value: String) : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        return value
    }

    override fun toString(): String {
        return "MessageElementString[" +
                "value='" + value + '\'' +
                "] " + super.toString()
    }
}

open class TypedMessageElement(protected var content: MessageElement) : MessageElement() {
    override fun toString(): String {
        return "TypedMessageElement[type=" + javaClass.name +
                ", content=" + content +
                "] " + super.toString()
    }
}

class MessageElementBold(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val s = SpannableString(content.makeSpan(context))
        s.setSpan(StyleSpan(Typeface.BOLD), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}

class MessageElementUser(private val userId: String, private val userDescription: String) : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val s = SpannableString(userDescription)
        s.setSpan(BackgroundColorSpan(Color.rgb(0xe3, 0xe3, 0xe3)), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }
}

class MessageElementUnknownType(private val type: String) : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        return "[TYPE=$type]"
    }

    override fun toString(): String {
        return "MessageElementUnknownType[type='$type']"
    }
}

class MessageHistoryResult {
    @SerializedName("messages")
    lateinit var messages: List<Message>

    override fun toString(): String {
        return "MessageHistoryResult[messages=$messages]"
    }
}

class MessageElementUrl(private val addr: String, private val description: String) : MessageElement() {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        val s = SpannableString(description)
        s.setSpan(URLSpan(addr), 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }

    override fun toString(): String {
        return "MessageElementUrl[addr='$addr', description='$description']"
    }
}

class MessageElementParagraph(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(context: SpanGenerationContext): CharSequence {
        return content.makeSpan(context)
    }
}

abstract class MessageElement : Serializable {
    open fun makeSpan(context: SpanGenerationContext): CharSequence {
        return "[NOT-IMPLEMENTED type=" + javaClass.name + "]"
    }

    class SpanGenerationContext(val context: Context)
}
