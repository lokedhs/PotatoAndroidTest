package com.dhsdevelopments.potato.clientapi.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings( "UnusedDeclaration" )
public class Message implements Serializable
{
    @SerializedName( "id" )
    public String id;

    @SerializedName( "channel" )
    public String channel;

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

    @SerializedName( "deleted" )
    public boolean deleted;

    @SerializedName( "hash" )
    public String hash;

    @SerializedName( "updated" )
    public Integer updated;

    @SerializedName( "extra_html" )
    public String extraHtml;

    @SerializedName( "star_users" )
    public List<String> starUsers;

    @SerializedName( "image" )
    public MessageImage messageImage;

    @Override
    public String toString() {
        return "Message[" +
                       "id='" + id + '\'' +
                       ", channel='" + channel + '\'' +
                       ", createdDate='" + createdDate + '\'' +
                       ", from='" + from + '\'' +
                       ", fromName='" + fromName + '\'' +
                       ", text=" + text +
                       ", useMath=" + useMath +
                       ", deleted=" + deleted +
                       ", hash='" + hash + '\'' +
                       ", updated=" + updated +
                       ", extraHtml='" + extraHtml + '\'' +
                       ", starUsers=" + starUsers +
                       ']';
    }
}
