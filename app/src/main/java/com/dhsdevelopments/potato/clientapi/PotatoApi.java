package com.dhsdevelopments.potato.clientapi;

import com.dhsdevelopments.potato.clientapi.channel.Domain;
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;

import java.util.List;

public interface PotatoApi
{
    @GET( "channels" )
    Call<List<Domain>> getChannels( @Header( "API-token" ) String apiKey );

    @GET( "channel/{cid}/history?format=json" )
    Call<MessageHistoryResult> loadHistoryAsJson( @Header( "API-token" ) String apiKey, @Path( "cid" ) String channelId, @Query( "num" ) int numMessages );
}
