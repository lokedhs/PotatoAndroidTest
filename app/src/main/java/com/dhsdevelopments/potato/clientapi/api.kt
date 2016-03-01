package com.dhsdevelopments.potato.clientapi

import android.content.Context
import android.net.Uri
import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult
import com.dhsdevelopments.potato.clientapi.command.SendCommandRequest
import com.dhsdevelopments.potato.clientapi.command.SendCommandResult
import com.dhsdevelopments.potato.clientapi.deletemessage.DeleteMessageResult
import com.dhsdevelopments.potato.clientapi.domainchannels.ChannelsInDomainResult
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult
import com.dhsdevelopments.potato.clientapi.search.SearchResult
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationRequest
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationResult
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.RequestBody
import okio.BufferedSink
import retrofit.Call
import retrofit.http.*
import java.io.IOException


interface PotatoApi {
    @GET("domain/{domainId}/channels")
    fun getAllChannelsInDomain(@Header("API-token") apiKey: String,
                               @Path("domainId") domainId: String): Call<ChannelsInDomainResult>

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
                       @Query("event-id") eventId: String?,
                       @Query("session_id") sid: String?): Call<PotatoNotificationResult>

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

    @POST("command")
    fun sendCommand(@Header("API-token") apiKey: String,
                    @Body request: SendCommandRequest): Call<SendCommandResult>
}

interface RemoteResult {
    fun errorMsg(): String?
}

fun plainErrorHandler(msg: String): Unit {
    throw RuntimeException("Error while performing remote call: $msg")
}

class ChannelUpdatesUpdateResult {
    @SerializedName("result")
    lateinit var result: String

    override fun toString(): String {
        return "ChannelUpdatesUpdateResult(result='$result')"
    }
}

class ClearNotificationsResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    override fun errorMsg(): String? {
        return if (result == "ok") null else result
    }

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
