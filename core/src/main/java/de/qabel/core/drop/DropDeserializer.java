package de.qabel.core.drop;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

@Deprecated
public class DropDeserializer implements JsonDeserializer<DropMessage> {
    @Override
    public DropMessage deserialize(JsonElement json, Type type,
                                   JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        int version = jsonObject.get("version").getAsInt();
        if (version != DropMessage.getVersion()) {
            throw new JsonParseException("Unexpected version: " + version);
        }
        long time = jsonObject.get("time_stamp").getAsLong();
        String sender = jsonObject.get("sender").getAsString();
        String acknowledgeID = jsonObject.get("acknowledge_id").getAsString();
        String dropPayload = jsonObject.get("drop_payload").getAsString();
        String dropPayloadType = jsonObject.get("drop_payload_type").getAsString();

        DropMessageMetadata dropMessageMetadata = null;
        if(jsonObject.has(DropSerializer.META_DATA)){
            dropMessageMetadata = context.deserialize(jsonObject.get(DropSerializer.META_DATA), DropMessageMetadata.class);
        }

        return new DropMessage(sender, dropPayload, dropPayloadType, new Date(time), acknowledgeID, dropMessageMetadata);
    }
}
