package de.qabel.core.drop;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.qabel.core.module.ModuleManager;


public class DropDeserializer implements JsonDeserializer<DropMessage<ModelObject>> {
    @Override
    public DropMessage<ModelObject> deserialize(JsonElement json, Type type,
                                                JsonDeserializationContext context) throws JsonParseException {

        int version          = json.getAsJsonObject().get("version")       .getAsInt();
        if (version != DropMessage.getVersion()) {
        	throw new JsonParseException("Unexpected version: " + version);
        }
        long time             = json.getAsJsonObject().get("time_stamp")    .getAsLong();
        String sender        = json.getAsJsonObject().get("sender")        .getAsString();
        String model         = json.getAsJsonObject().get("model_object")  .getAsString();
        String acknowledgeID = json.getAsJsonObject().get("acknowledge_id").getAsString();

        ModelObject m;
        try {
            ClassLoader loader = ModuleManager.LOADER;

            @SuppressWarnings("unchecked")
            Class<? extends ModelObject> cls = (Class<? extends ModelObject>) loader
                    .loadClass(model);
            m = new Gson().fromJson(json.getAsJsonObject().get("data").getAsJsonObject(), cls);
        } catch (ClassNotFoundException e1) {
            throw new JsonParseException("Couldn't deserialize 'data' entry", e1);
        }

        return new DropMessage<>(
        		sender,
        		m,
        		new Date(time),
        		acknowledgeID);
    }
}
