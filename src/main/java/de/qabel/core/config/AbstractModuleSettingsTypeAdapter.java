package de.qabel.core.config;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public abstract class AbstractModuleSettingsTypeAdapter<T extends AbstractModuleSettings> implements
		JsonDeserializer<T>, JsonSerializer<T> {
	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		root.addProperty("module_name", src.getClass().getCanonicalName());
		root.add("settings", context.serialize(src, src.getClass()));
		return root;
	}

	@Override
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		String moduleName = json.getAsJsonObject().get("module_name").getAsString();
		Class<?> settingsClass;
		try {
			settingsClass = Class.forName(moduleName);
		} catch (ClassNotFoundException e) {
			throw new JsonParseException("Couldn't load module settings class", e);
		}
		return context.deserialize(json.getAsJsonObject().get("settings"), settingsClass);
	}
}
