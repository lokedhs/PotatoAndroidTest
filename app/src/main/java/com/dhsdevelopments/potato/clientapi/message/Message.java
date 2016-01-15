package com.dhsdevelopments.potato.clientapi.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings( "UnusedDeclaration" )
public class Message implements Serializable
{
    @SerializedName( "id" )
    public String id;

    @SerializedName( "created_date" )
    public String createdDate;

    @SerializedName( "from" )
    public String from;

    @SerializedName( "from_name" )
    public String fromName;

    @SerializedName( "text" )
    public MessageElement text;

    @SerializedName( "use_math" )
    public boolean useMath;

    @SerializedName( "update" )
    public MessageUpdate update;

    public String type;

    public String getChannel() {
        String namePrefix = "msg-";
        if( !type.startsWith( namePrefix ) ) {
            throw new IllegalStateException( "Type for message does not start with '" + namePrefix + "'" );
        }
        return type.substring( namePrefix.length() );
    }

    @Override
    public String toString() {
        return "Message[" +
                       "id='" + id + '\'' +
                       ", createdDate='" + createdDate + '\'' +
                       ", from='" + from + '\'' +
                       ", fromName='" + fromName + '\'' +
                       ", text=" + text +
                       ", useMath=" + useMath +
                       ", update='" + update + '\'' +
                       ", type='" + type + '\'' +
                       ']';
    }
}
