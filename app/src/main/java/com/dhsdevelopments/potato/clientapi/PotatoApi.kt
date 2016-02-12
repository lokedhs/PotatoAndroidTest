package com.dhsdevelopments.potato.clientapi

import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult
import com.dhsdevelopments.potato.clientapi.deletemessage.DeleteMessageResult
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult
import com.dhsdevelopments.potato.clientapi.search.SearchResult
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationRequest
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationResult
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult
import retrofit.Call
import retrofit.http.*

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
