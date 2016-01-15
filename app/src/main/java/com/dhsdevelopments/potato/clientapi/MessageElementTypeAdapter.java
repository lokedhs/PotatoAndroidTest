package com.dhsdevelopments.potato.clientapi;

import com.dhsdevelopments.potato.clientapi.message.*;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MessageElementTypeAdapter implements JsonDeserializer<MessageElement>
{
    @Override
    public MessageElement deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        if( json.isJsonPrimitive() ) {
            return new MessageElementString( json.getAsString() );
        }
        else if( json.isJsonArray() ) {
            JsonArray a = json.getAsJsonArray();
            List<MessageElement> result = new ArrayList<>( a.size() );
            for( JsonElement v : a ) {
                MessageElement element = context.deserialize( v, MessageElement.class );
                result.add( element );
            }
            return new MessageElementList( result );
        }
        else {
            JsonObject obj = json.getAsJsonObject();

            String type = obj.get( "type" ).getAsString();
            switch( type ) {
                case "p": {
                    JsonElement element = obj.get( "e" );
                    MessageElement messageElement = context.deserialize( element, MessageElement.class );
                    return new MessageElementParagraph( messageElement );
                }
                case "b": {
                    JsonElement element = obj.get( "e" );
                    MessageElement messageElement = context.deserialize( element, MessageElement.class );
                    return new MessageElementBold( messageElement );
                }
                case "i": {
                    JsonElement element = obj.get( "e" );
                    MessageElement messageElement = context.deserialize( element, MessageElement.class );
                    return new MessageElementItalics( messageElement );
                }
                case "code": {
                    JsonElement element = obj.get( "e" );
                    MessageElement messageElement = context.deserialize( element, MessageElement.class );
                    return new MessageElementCode( messageElement );
                }
                case "url":
                    String addr = obj.get( "addr" ).getAsString();
                    JsonElement description = obj.get( "description" );
                    return new MessageElementUrl( addr, description.isJsonNull() ? addr : description.getAsString() );
                case "code-block":
                    String language = obj.get( "language" ).getAsString();
                    String code = obj.get( "code" ).getAsString();
                    return new MessageElementCodeBlock( language, code );
                case "user":
                    String userId = obj.get( "user_id" ).getAsString();
                    String userDescription = obj.get( "user_description" ).getAsString();
                    return new MessageElementUser( userId, userDescription );
                default:
                    return new MessageElementUnknownType( type );
            }
        }
    }
}
