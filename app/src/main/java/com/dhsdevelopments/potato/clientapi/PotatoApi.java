package com.dhsdevelopments.potato.clientapi;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;

import java.util.List;

public interface PotatoApi
{
    @GET( "channels" )
    Call<List<Domain>> getChannels( @Header( "API-token" ) String apiKey );

}
