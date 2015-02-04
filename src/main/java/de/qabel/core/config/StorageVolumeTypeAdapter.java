package de.qabel.core.config;

import java.io.IOException;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class StorageVolumeTypeAdapter extends TypeAdapter<StorageVolume> {
	private final TypeAdapter<StorageVolume> defaultAdapter = new GsonBuilder()
			.setExclusionStrategies(new ExcludeServerStrategy())
			.create().getAdapter(StorageVolume.class);

	@Override
	public void write(JsonWriter out, StorageVolume value) throws IOException {
		defaultAdapter.write(out, value);
	}

	@Override
	public StorageVolume read(JsonReader in) throws IOException {
		return defaultAdapter.read(in);
	}

	private class ExcludeServerStrategy implements ExclusionStrategy {
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return f.getDeclaredType() == StorageServer.class;
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}
}
