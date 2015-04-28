package de.qabel.core.config;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AbstractModuleSettingsTypeAdapter implements JsonDeserializer<AbstractModuleSettings>, JsonSerializer<AbstractModuleSettings> {
	@Override
	public JsonElement serialize(AbstractModuleSettings src, Type typeOfSrc, JsonSerializationContext context) {
		JsonElement elem = context.serialize(src, src.getClass());
		elem.getAsJsonObject().addProperty("module_name", src.getClass().getCanonicalName());
		return elem;
	}

	@Override
	public AbstractModuleSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		String moduleName = json.getAsJsonObject().get("module_name").getAsString();
		Class<?> settingsClass;
		try {
			settingsClass = Class.forName(moduleName);
		} catch (ClassNotFoundException e) {
			throw new JsonParseException("Couldn't load module settings class", e);
		}
		return context.deserialize(json, settingsClass);
	}
}
