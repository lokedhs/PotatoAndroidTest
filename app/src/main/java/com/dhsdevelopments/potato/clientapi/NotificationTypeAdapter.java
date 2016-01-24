package com.dhsdevelopments.potato.clientapi;

import com.dhsdevelopments.potato.clientapi.notifications.MessageNotification;
import com.dhsdevelopments.potato.clientapi.notifications.PotatoNotification;
import com.dhsdevelopments.potato.clientapi.notifications.StateUpdateNotification;
import com.dhsdevelopments.potato.clientapi.notifications.TypingNotification;
import com.google.gson.*;

import java.lang.reflect.Type;

public class NotificationTypeAdapter implements JsonDeserializer<PotatoNotification>
{
    @Override
    public PotatoNotification deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String type = obj.get( "type" ).getAsString();
        switch( type ) {
            case "m":
                return context.deserialize( obj, MessageNotification.class );
            case "cu":
                return context.deserialize( obj, StateUpdateNotification.class );
            case "type":
                return context.deserialize( obj, TypingNotification.class );
            default:
                return context.deserialize( obj, PotatoNotification.class );
        }
    }
}
