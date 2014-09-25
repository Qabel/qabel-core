package de.qabel.core.drop;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


public class DropDeserializer implements JsonDeserializer<DropMessage<ModelObject>> {
    @Override
    public DropMessage<ModelObject> deserialize(JsonElement json, Type type,
                                                JsonDeserializationContext context) throws JsonParseException {

        int version          = json.getAsJsonObject().get("version")      .getAsInt();
        int time             = json.getAsJsonObject().get("time")         .getAsInt();
        String sender        = json.getAsJsonObject().get("sender")       .getAsString();
        String model         = json.getAsJsonObject().get("model")        .getAsString();
        String acknowledgeID = json.getAsJsonObject().get("acknowledgeID").getAsString();

        ModelObject m;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();

            @SuppressWarnings("unchecked")
            Class<? extends ModelObject> cls = (Class<? extends ModelObject>) loader
                    .loadClass(model);
            m = new Gson().fromJson(json.getAsJsonObject(), cls);
        } catch (ClassNotFoundException e1) {
            //not found
            return null;
        }
        DropMessage<ModelObject> dm = new DropMessage<ModelObject>();

        dm.setVersion(version);
        dm.setTime(new Date(time));
        dm.setSender(sender);
        dm.setData(m);

        return dm;
    }
}
