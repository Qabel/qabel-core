package de.qabel.core.drop;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DropDeserializer implements JsonDeserializer<DropMessage> {
    @Override
    public DropMessage deserialize(JsonElement json, Type type,
                                                JsonDeserializationContext context) throws JsonParseException {

        int version          = json.getAsJsonObject().get("version")       .getAsInt();
        if (version != DropMessage.getVersion()) {
        	throw new JsonParseException("Unexpected version: " + version);
        }
        long time             = json.getAsJsonObject().get("time_stamp")    .getAsLong();
        String sender        = json.getAsJsonObject().get("sender")        .getAsString();
        String acknowledgeID = json.getAsJsonObject().get("acknowledge_id").getAsString();
        String dropPayload   = json.getAsJsonObject().get("drop_payload")  .getAsString();
        String dropPayloadType = json.getAsJsonObject().get("drop_payload_type").getAsString();

        return new DropMessage(sender, dropPayload, dropPayloadType, new Date(time), acknowledgeID);
    }
}
