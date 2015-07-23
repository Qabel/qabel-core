package de.qabel.core.drop;


import com.google.gson.*;

import java.lang.reflect.Type;

public class DropSerializer implements JsonSerializer<DropMessage> {
    @Override
    public JsonElement serialize (DropMessage src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject obj = new JsonObject();
        Gson gson = new Gson();

        obj.addProperty("version",        DropMessage.getVersion());
        obj.addProperty("time_stamp",     src.getCreationDate().getTime());
        obj.addProperty("sender",         src.getSender().getKeyIdentifier());
        obj.addProperty("acknowledge_id", src.getAcknowledgeID());
        obj.add("drop_payload",            gson.toJsonTree(src.getDropPayload()));
        obj.add("drop_payload_type", 	gson.toJsonTree(src.getDropPayloadType()));

        return obj;
    }
}