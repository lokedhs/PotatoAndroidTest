package com.dhsdevelopments.potato.clientapi

import android.content.Context
import android.net.Uri
import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult
import com.dhsdevelopments.potato.clientapi.deletemessage.DeleteMessageResult
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult
import com.dhsdevelopments.potato.clientapi.message.*
import com.dhsdevelopments.potato.clientapi.notifications.*
import com.dhsdevelopments.potato.clientapi.search.SearchResult
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationRequest
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationResult
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.RequestBody
import okio.BufferedSink
import retrofit.Call
import retrofit.http.*
import java.io.IOException
import java.lang.reflect.Type
import java.util.*


interface PotatoApi {
    @GET("channels2")
    fun getChannels2(@Header("API-token") apiKey: String): Call<ChannelsResult>

    @GET("channel/{cid}/history?format=json")
    fun loadHistoryAsJson(@Header("API-token") apiKey: String,
                          @Path("cid") channelId: String,
                          @Query("num") numMessages: Int,
                          @Query("from") from: String): Call<MessageHistoryResult>

    @GET("channel-updates?format=json")
    fun channelUpdates(@Header("API-token") apiKey: String,
                       @Query("channels") channels: String,
                       @Query("services") services: String,
                       @Query("event-id") eventId: String?): Call<PotatoNotificationResult>

    @POST("channel-updates/update")
    fun channelUpdatesUpdate(@Header("API-token") apiKey: String,
                             @Query("event-id") eventId: String,
                             @Query("cmd") cmd: String,
                             @Query("channel") channelId: String,
                             @Query("services") services: String): Call<ChannelUpdatesUpdateResult>

    @POST("channel/{cid}/create")
    fun sendMessage(@Header("API-token") apiKey: String,
                    @Path("cid") channelId: String,
                    @Body request: SendMessageRequest): Call<SendMessageResult>

    @DELETE("message/{messageId}")
    fun deleteMessage(@Header("API-token") apiKey: String,
                      @Path("messageId") messageId: String): Call<DeleteMessageResult>

    @Multipart
    @POST("channel/{cid}/upload")
    fun sendMessageWithFile(@Header("API-token") apiKey: String,
                            @Path("cid") channelId: String,
                            @PartMap params: Map<String, Any>): Call<SendMessageResult>

    @GET("channel/{cid}/users")
    fun loadUsers(@Header("API-token") apiKey: String,
                  @Path("cid") channelId: String): Call<LoadUsersResult>

    @POST("register-gcm")
    fun registerGcm(@Header("API-token") apiKey: String,
                    @Body request: GcmRegistrationRequest): Call<GcmRegistrationResult>

    @POST("channel/{cid}/clear-notifications")
    fun clearNotificationsForChannel(@Header("API-token") apiKey: String,
                                     @Path("cid") channelId: String): Call<ClearNotificationsResult>

    @POST("channel/{cid}/unread-notification")
    fun updateUnreadNotification(@Header("API-token") apiKey: String,
                                 @Path("cid") channelId: String,
                                 @Body request: UpdateUnreadNotificationRequest): Call<UpdateUnreadNotificationResult>

    @GET("channel/{cid}/search")
    fun searchMessages(@Header("API-token") apiKey: String,
                       @Path("cid") channelId: String,
                       @Query("query") query: String,
                       @Query("star-only") starOnly: String): Call<SearchResult>
}

class ChannelUpdatesUpdateResult {
    @SerializedName("result")
    lateinit var result: String

    override fun toString(): String {
        return "ChannelUpdatesUpdateResult(result='$result')"
    }
}

class ClearNotificationsResult {
    @SerializedName("result")
    lateinit var result: String

    override fun toString(): String {
        return "ClearNotificationsResult[result='$result']"
    }
}

class ImageUriRequestBody(private val context: Context, private val imageUri: Uri) : RequestBody() {
    private val mediaType: MediaType

    init {
        this.mediaType = MediaType.parse(context.contentResolver.getType(imageUri))
    }

    override fun contentType(): MediaType {
        return mediaType
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(imageUri).use { inStream ->
            val out = sink.outputStream()
            val buf = ByteArray(16 * 1024)
            while (true) {
                val n = inStream.read(buf)
                if (n == -1) {
                    break
                }
                out.write(buf, 0, n)
            }
        }
    }
}

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

class NotificationTypeAdapter : JsonDeserializer<PotatoNotification> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PotatoNotification {
        val obj = json.asJsonObject
        val type = obj.get("type").asString
        return when (type) {
            "m" -> context.deserialize<PotatoNotification>(obj, MessageNotification::class.java)
            "cu" -> context.deserialize<PotatoNotification>(obj, StateUpdateNotification::class.java)
            "type" -> context.deserialize<PotatoNotification>(obj, TypingNotification::class.java)
            else -> context.deserialize<PotatoNotification>(obj, PotatoNotification::class.java)
        }
    }
}
