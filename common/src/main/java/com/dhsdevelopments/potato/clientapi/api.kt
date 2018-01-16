package com.dhsdevelopments.potato.clientapi

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult
import com.dhsdevelopments.potato.clientapi.channel2.FindPrivateChannelIdResult
import com.dhsdevelopments.potato.clientapi.channelinfo.CreateChannelRequest
import com.dhsdevelopments.potato.clientapi.channelinfo.LoadChannelInfoResult
import com.dhsdevelopments.potato.clientapi.command.SendCommandRequest
import com.dhsdevelopments.potato.clientapi.command.SendCommandResult
import com.dhsdevelopments.potato.clientapi.deletemessage.DeleteMessageResult
import com.dhsdevelopments.potato.clientapi.domainchannels.DomainInfoResult
import com.dhsdevelopments.potato.clientapi.editchannel.LeaveChannelResult
import com.dhsdevelopments.potato.clientapi.editchannel.UpdateChannelVisibilityRequest
import com.dhsdevelopments.potato.clientapi.editchannel.UpdateChannelVisibilityResult
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult
import com.dhsdevelopments.potato.clientapi.search.SearchResult
import com.dhsdevelopments.potato.clientapi.sendmessage.unreadnotification.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.unreadnotification.SendMessageResult
import com.dhsdevelopments.potato.clientapi.users.LoadUserResult
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult
import com.dhsdevelopments.potato.common.Log
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.RequestBody
import okio.BufferedSink
import retrofit.Call
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import retrofit.http.*
import java.io.IOException


interface PotatoApi {
    //    @GET("domain/{domainId}/channels")
    //    fun getAllChannelsInDomain(@Header("API-token") apiKey: String,
    //                               @Path("domainId") domainId: String): Call<ChannelsInDomainResult>

    @GET("domains/{domainId}")
    fun getAllChannelsInDomain(@Header("API-token") apiKey: String,
                               @Path("domainId") domainId: String,
                               @Query("include-groups") includeGroups: String,
                               @Query("include-channels") includeChannels: String): Call<DomainInfoResult>

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

    @GET("channel/{cid}")
    fun loadChannelInfo(@Header("API-token") apiKey: String,
                        @Path("cid") channelId: String): Call<LoadChannelInfoResult>

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

    @PUT("channel/create")
    fun createChannel(@Header("API-token") apiKey: String,
                      @Body request: CreateChannelRequest): Call<LoadChannelInfoResult>

    @GET("users/{uid}")
    fun loadUser(@Header("API-token") apiKey: String,
                 @Path("uid") userId: String): Call<LoadUserResult>

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

    @POST("channel/{cid}/leave")
    fun leaveChannel(@Header("API-token") apiKey: String,
                     @Path("cid") channelId: String): Call<LeaveChannelResult>

    @POST("channel/{cid}/show")
    fun updateChannelVisibility(@Header("API-token") apiKey: String,
                                @Path("cid") channelId: String,
                                @Body request: UpdateChannelVisibilityRequest): Call<UpdateChannelVisibilityResult>

    @POST("private/{domainId}/{uid}")
    fun findPrivateChannelId(@Header("API-token") apiKey: String,
                             @Path("domainId") domainId: String,
                             @Path("uid") userId: String): Call<FindPrivateChannelIdResult>
}

interface RemoteResult {
    /**
     * This function should return null if the operation was successful.
     */
    fun errorMsg(): String?
}

fun plainErrorHandler(msg: String) {
    throw RuntimeException("Error while performing remote call: $msg")
}

class ChannelUpdatesUpdateResult {
    @SerializedName("result")
    lateinit var result: String

    override fun toString(): String = "ChannelUpdatesUpdateResult(result='$result')"
}

class ClearNotificationsResult : RemoteResult {
    @SerializedName("result")
    lateinit var result: String

    override fun errorMsg() = if (result == "ok") null else result

    override fun toString(): String = "ClearNotificationsResult[result='$result']"
}

class ImageUriRequestBody(private val context: Context, private val imageUri: Uri) : RequestBody() {
    private val mediaType: MediaType = MediaType.parse(context.contentResolver.getType(imageUri))

    override fun contentType(): MediaType = mediaType

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

fun <T : RemoteResult> callService(call: Call<T>, errorCallback: (String) -> Unit, successCallback: (T) -> Unit) {
    val result = call.execute()
    if (result.isSuccess) {
        val body = result.body()
        val errMsg = body.errorMsg()
        if (errMsg == null) {
            successCallback(body)
        }
        else {
            errorCallback(errMsg)
        }
    }
    else {
        errorCallback("Call failed, code: ${result.code()}, message: ${result.message()}")
    }
}

fun <T : RemoteResult> callServiceBackground(call: Call<T>, errorCallback: (String) -> Unit, successCallback: (T) -> Unit) {
    val handler = Handler()
    call.enqueue(object : Callback<T> {
        override fun onResponse(response: Response<T>, retrofit: Retrofit) {
            handler.post {
                if (response.isSuccess) {
                    val errorMessage = response.body().errorMsg()
                    if (errorMessage == null) {
                        successCallback(response.body())
                    }
                    else {
                        errorCallback(errorMessage)
                    }
                }
                else {
                    errorCallback("Call failed, code: ${response.code()}, message: ${response.message()}")
                }
            }
        }

        override fun onFailure(exception: Throwable) {
            Log.e("Exception when calling remote service", exception)
            handler.post {
                errorCallback("Connection error: ${exception.message}")
            }
        }
    })
}
