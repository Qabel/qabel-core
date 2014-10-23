package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class StorageServersTypeAdapter extends TypeAdapter<StorageServers> {

	@Override
	public void write(JsonWriter out, StorageServers value) throws IOException {
		out.beginArray();
		Gson gson = new Gson();
		Set<StorageServer> set = value.getStorageServer();
		TypeAdapter<StorageServer> adapter = gson.getAdapter(StorageServer.class);
		for(StorageServer storageServer : set) {
			adapter.write(out, storageServer);
		}
		out.endArray();
		return;
	}

	@Override
	public StorageServers read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		Gson gson = new Gson();
		StorageServers storageServers = new StorageServers();
		TypeAdapter<StorageServer> adapter = gson.getAdapter(StorageServer.class);
		StorageServer storageServer = null; 
		
		in.beginArray();
		while(in.hasNext()) {
			storageServer = adapter.read(in);
			storageServers.add(storageServer);
		}
		in.endArray();
		
		return storageServers;
	}
}