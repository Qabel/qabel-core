package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class StorageVolumesTypeAdapter extends TypeAdapter<StorageVolumes> {

	@Override
	public void write(JsonWriter out, StorageVolumes value) throws IOException {
		out.beginArray();
		Set<StorageVolume> set = value.getStorageVolumes();
		TypeAdapter<StorageVolume> adapter = new StorageVolumeTypeAdapter();
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
		
		StorageVolumes storageVolumes = new StorageVolumes();
		TypeAdapter<StorageVolume> adapter = new StorageVolumeTypeAdapter();
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
