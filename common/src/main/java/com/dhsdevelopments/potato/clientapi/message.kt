@file:Suppress("PackageDirectoryMismatch")

package com.dhsdevelopments.potato.clientapi.message

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import com.dhsdevelopments.potato.common.R
import com.dhsdevelopments.potato.messagedisplay.CodeBlockBackgroundSpan
import com.dhsdevelopments.potato.messagedisplay.CodeBlockTypefaceSpan
import com.dhsdevelopments.potato.messagedisplay.CodeTypefaceSpan
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.reflect.Type
import java.util.*

abstract class MessageElement : Serializable {
    open fun makeSpan(spanContext: SpanGenerationContext): CharSequence = "[NOT-IMPLEMENTED type=" + javaClass.name + "]"

    class SpanGenerationContext(val context: Context)
}

class MessageElementTypeAdapter : JsonDeserializer<MessageElement> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MessageElement {
        return when {
            json.isJsonPrimitive -> {
                MessageElementString(json.asString)
            }
            json.isJsonArray -> {
                val a = json.asJsonArray
                val result = ArrayList<MessageElement>(a.size())
                a.mapTo(result) { context.deserialize<MessageElement>(it, MessageElement::class.java) }
                MessageElementList(result)
            }
            else -> {
                val obj = json.asJsonObject
                fun makeElement() = context.deserialize<MessageElement>(obj.get("e"), MessageElement::class.java)

                val type = obj.get("type").asString
                when (type) {
                    "p" -> MessageElementParagraph(makeElement())
                    "b" -> MessageElementBold(makeElement())
                    "i" -> MessageElementItalics(makeElement())
                    "code" -> MessageElementCode(makeElement())
                    "math" -> MessageElementMath(makeElement())
                    "inline-math" -> MessageElementInlineMath(makeElement())
                    "code-block" -> MessageElementCodeBlock(obj.get("language").asString, obj.get("code").asString)
                    "user" -> MessageElementUser(obj.get("user_id").asString, obj.get("user_description").asString)
                    "newline" -> MessageElementNewline()
                    "url" -> {
                        val addr = obj.get("addr").asString
                        val description = obj.get("description")
                        MessageElementUrl(addr, if (description.isJsonNull) addr else description.asString)
                    }
                    else -> MessageElementUnknownType(type)
                }
            }
        }
    }
}

class MessageElementList(private val list: List<MessageElement>) : MessageElement() {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        val builder = SpannableStringBuilder()
        for (element in list) {
            builder.append(element.makeSpan(spanContext))
        }
        return builder
    }

    override fun toString(): String {
        return "MessageElementList[" +
                "list=" + list +
                "] " + super.toString()
    }
}

@Suppress("MemberVisibilityCanPrivate")
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
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        return SpannableString(content.makeSpan(spanContext)).apply {
            setSpan(CodeTypefaceSpan(spanContext.context), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

class MessageElementCodeBlock(private val language: String, private val code: String) : MessageElement() {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        val adjustedString = "\n" + code + "\n"
        return SpannableString(adjustedString).apply {
            setSpan(CodeBlockTypefaceSpan(), 1, adjustedString.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(CodeBlockBackgroundSpan(spanContext.context), 1, adjustedString.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun toString() = "MessageElementCodeBlock[language='$language', code='$code'] ${super.toString()}"
}

class MessageElementMath(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        return "[math: " + content.makeSpan(spanContext).toString() + "]"
    }
}

class MessageElementInlineMath(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        return "[math: " + content.makeSpan(spanContext).toString() + "]"
    }
}

class MessageElementNewline : MessageElement() {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence = "\n"
}

class MessageElementItalics(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        val s = SpannableString(content.makeSpan(spanContext))
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
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence = value

    override fun toString() = "MessageElementString[value='$value'] ${super.toString()}"
}

open class TypedMessageElement(protected val content: MessageElement) : MessageElement() {
    override fun toString() = "TypedMessageElement[type=${javaClass.name}, content=$content] ${super.toString()}"
}

class MessageElementBold(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        return SpannableString(content.makeSpan(spanContext)).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

class MessageElementUser(private val userId: String, private val userDescription: String) : MessageElement() {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        return SpannableString(userDescription).apply {
            val colour = ContextCompat.getColor(spanContext.context, R.color.user_background)
            setSpan(BackgroundColorSpan(colour), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

class MessageElementUnknownType(private val type: String) : MessageElement() {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence = "[TYPE=$type]"

    override fun toString() = "MessageElementUnknownType[type='$type']"
}

class MessageHistoryResult {
    @SerializedName("messages")
    lateinit var messages: List<Message>

    override fun toString() = "MessageHistoryResult[messages=$messages]"
}

class MessageElementUrl(private val addr: String, private val description: String) : MessageElement() {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence {
        return SpannableString(description).apply {
            setSpan(URLSpan(addr), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun toString() = "MessageElementUrl[addr='$addr', description='$description']"
}

class MessageElementParagraph(content: MessageElement) : TypedMessageElement(content) {
    override fun makeSpan(spanContext: SpanGenerationContext): CharSequence = content.makeSpan(spanContext)
}
