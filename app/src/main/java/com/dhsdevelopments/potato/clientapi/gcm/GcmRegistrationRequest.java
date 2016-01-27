package com.dhsdevelopments.potato.clientapi.gcm;

import com.google.gson.annotations.SerializedName;

public class GcmRegistrationRequest
{
    @SerializedName( "token" )
    public String token;

    public GcmRegistrationRequest() {
    }

    public GcmRegistrationRequest( String token ) {
        this.token = token;
    }
}
