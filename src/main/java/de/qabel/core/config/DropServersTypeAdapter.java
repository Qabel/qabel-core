package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class DropServersTypeAdapter extends TypeAdapter<DropServers> {

	@Override
	public void write(JsonWriter out, DropServers value) throws IOException {
		out.beginArray();
		Gson gson = new Gson();
		Set<DropServer> set = value.getDropServers();
		TypeAdapter<DropServer> adapter = gson.getAdapter(DropServer.class);
		for(DropServer dropServer : set) {
			adapter.write(out, dropServer);
		}
		out.endArray();
		return;
	}

	@Override
	public DropServers read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		Gson gson = new Gson();
		DropServers dropServers = new DropServers();
		TypeAdapter<DropServer> adapter = gson.getAdapter(DropServer.class);
		DropServer dropServer = null; 
		
		in.beginArray();
		while(in.hasNext()) {
			dropServer = adapter.read(in);
			dropServers.put(dropServer);
		}
		in.endArray();
		
		return dropServers;
	}
}
