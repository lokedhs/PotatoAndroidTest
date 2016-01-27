package com.dhsdevelopments.potato.clientapi;

import com.dhsdevelopments.potato.clientapi.channel.Domain;
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationRequest;
import com.dhsdevelopments.potato.clientapi.gcm.GcmRegistrationResult;
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotificationResult;
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest;
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult;
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult;
import retrofit.Call;
import retrofit.http.*;

import java.util.List;

public interface PotatoApi
{
    @GET( "channels" )
    Call<List<Domain>> getChannels( @Header( "API-token" ) String apiKey );

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

    @GET( "channel/{cid}/users" )
    Call<LoadUsersResult> loadUsers( @Header( "API-token" ) String apiKey,
                                     @Path( "cid" ) String channelId );

    @POST( "register-gcm" )
    Call<GcmRegistrationResult> registerGcm( @Header( "API-token" ) String apiKey,
                                             @Body GcmRegistrationRequest request );
}
