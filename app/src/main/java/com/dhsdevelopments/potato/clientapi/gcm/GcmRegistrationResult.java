package com.dhsdevelopments.potato.clientapi.gcm;

import com.google.gson.annotations.SerializedName;

public class GcmRegistrationResult
{
    @SerializedName( "result" )
    public String result;

    @SerializedName( "detail" )
    public String detail;
}
