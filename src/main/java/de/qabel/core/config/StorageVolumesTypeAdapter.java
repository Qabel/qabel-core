package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class StorageVolumesTypeAdapter extends TypeAdapter<StorageVolumes> {

	@Override
	public void write(JsonWriter out, StorageVolumes value) throws IOException {
		out.beginArray();
		Gson gson = new Gson();
		Set<StorageVolume> set = value.getStorageVolumes();
		TypeAdapter<StorageVolume> adapter = gson.getAdapter(StorageVolume.class);
		for(StorageVolume storageVolume : set) {
			adapter.write(out, storageVolume);
		}
		out.endArray();
		return;
	}

	@Override
	public StorageVolumes read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		Gson gson = new Gson();
		StorageVolumes storageVolumes = new StorageVolumes();
		TypeAdapter<StorageVolume> adapter = gson.getAdapter(StorageVolume.class);
		StorageVolume storageVolume = null; 
		
		in.beginArray();
		while(in.hasNext()) {
			storageVolume = adapter.read(in);
			storageVolumes.add(storageVolume);
		}
		in.endArray();
		
		return storageVolumes;
	}
}
