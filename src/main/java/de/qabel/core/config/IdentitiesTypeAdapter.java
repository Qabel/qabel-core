package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class IdentitiesTypeAdapter extends TypeAdapter<Identities> {

	@Override
	public void write(JsonWriter out, Identities value) throws IOException {
		out.beginArray();
		Gson gson = new Gson();
		Set<Identity> set = value.getIdentities();
		TypeAdapter<Identity> adapter = gson.getAdapter(Identity.class);
		for(Identity identity : set) {
			adapter.write(out, identity);
		}
		out.endArray();
		return;
	}

	@Override
	public Identities read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		Gson gson = new Gson();
		Identities identities = new Identities();
		TypeAdapter<Identity> adapter = gson.getAdapter(Identity.class);
		Identity identity = null; 
		
		in.beginArray();
		while(in.hasNext()) {
			identity = adapter.read(in);
			identities.getIdentities().add(identity);
		}
		in.endArray();
		
		return identities;
	}
}