package de.qabel.core.drop;


import com.google.gson.*;

import java.lang.reflect.Type;

public class DropSerializer implements JsonSerializer<DropMessage<? extends ModelObject>> {
    public JsonElement serialize (DropMessage<? extends ModelObject> src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject obj = new JsonObject();
        Gson gson = new Gson();
        String model = src.getData().getClass().getName() + "";

        obj.addProperty("version",        src.getVersion());
        obj.addProperty("time_stamp",     src.getTime());
        obj.addProperty("sender",         src.getSender());
        obj.addProperty("acknowledge_id", src.getAcknowledgeID());
        obj.addProperty("model_object",   model);
        obj.add("data",                   gson.toJsonTree(src.getData()));

        return obj;
    }
}