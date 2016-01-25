package com.dhsdevelopments.potato.clientapi.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MessageImage implements Serializable
{
    @SerializedName( "file" )
    public String file;

    @SerializedName( "width" )
    public int width;

    @SerializedName( "height" )
    public int height;
}
