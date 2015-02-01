package de.qabel.core.drop;


import com.google.gson.*;

import java.lang.reflect.Type;

public class DropSerializer implements JsonSerializer<DropMessage<? extends ModelObject>> {
    public JsonElement serialize (DropMessage<? extends ModelObject> src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject obj = new JsonObject();
        Gson gson = new Gson();
        String model = src.getData().getClass().getName();

        obj.addProperty("version",        DropMessage.getVersion());
        obj.addProperty("time_stamp",     src.getCreationDate().getTime());
        obj.addProperty("sender",         src.getSender().getKeyIdentifier());
        obj.addProperty("acknowledge_id", src.getAcknowledgeID());
        obj.addProperty("model_object",   model);
        obj.add("data",                   gson.toJsonTree(src.getData()));

        return obj;
    }
}