package com.dhsdevelopments.potato.clientapi;

import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult;
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest;
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult;
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult;
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest;
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult;
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationRequest;
import com.dhsdevelopments.potato.clientapi.unreadnotification.UpdateUnreadNotificationResult;
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult;
import retrofit.Call;
import retrofit.http.*;

import java.util.Map;

public interface PotatoApi
{
    @GET( "channels2" )
    Call<ChannelsResult> getChannels2( @Header( "API-token" ) String apiKey );

    @GET( "channel/{cid}/history?format=json" )
    Call<MessageHistoryResult> loadHistoryAsJson( @Header( "API-token" ) String apiKey,
                                                  @Path( "cid" ) String channelId,
                                                  @Query( "num" ) int numMessages );

    @GET( "channel-updates?format=json" )
    Call<PotatoNotificationResult> channelUpdates( @Header( "API-token" ) String apiKey,
                                                   @Query( "channels" ) String channels,
                                                   @Query( "services" ) String services,
                                                   @Query( "event-id" ) String eventId );

    @POST( "channel-updates/update" )
    Call<ChannelUpdatesUpdateResult> channelUpdatesUpdate( @Header( "API-token" ) String apiKey,
                                                           @Query( "event-id" ) String eventId,
                                                           @Query( "cmd" ) String cmd,
                                                           @Query( "channel" ) String channelId,
                                                           @Query( "services" ) String services );

    @POST( "channel/{cid}/create" )
    Call<SendMessageResult> sendMessage( @Header( "API-token" ) String apiKey,
                                         @Path( "cid" ) String channelId,
                                         @Body SendMessageRequest request );

//    @Multipart
//    @POST( "channel/{cid}/upload" )
//    Call<SendMessageResult> sendMessageWithFile( @Header( "API-token" ) String apiKey,
//                                                 @Path( "cid" ) String channelId,
//                                                 @Part("content") SendMessageRequest request,
//                                                 @Part("body") RequestBody fileBody );

    @Multipart
    @POST( "channel/{cid}/upload" )
    Call<SendMessageResult> sendMessageWithFile( @Header( "API-token" ) String apiKey,
                                                 @Path( "cid" ) String channelId,
                                                 @PartMap Map<String, Object> params );

    @GET( "channel/{cid}/users" )
    Call<LoadUsersResult> loadUsers( @Header( "API-token" ) String apiKey,
                                     @Path( "cid" ) String channelId );

    @POST( "register-gcm" )
    Call<GcmRegistrationResult> registerGcm( @Header( "API-token" ) String apiKey,
                                             @Body GcmRegistrationRequest request );

    @POST( "channel/{cid}/clear-notifications" )
    Call<ClearNotificationsResult> clearNotificationsForChannel( @Header( "API-token" ) String apiKey,
                                                                 @Path( "cid" ) String channelId );

    @POST( "channel/{cid}/unread-notification" )
    Call<UpdateUnreadNotificationResult> updateUnreadNotification( @Header( "API-token" ) String apiKey,
                                                                   @Path( "cid" ) String channelId,
                                                                   @Body UpdateUnreadNotificationRequest request );
}
