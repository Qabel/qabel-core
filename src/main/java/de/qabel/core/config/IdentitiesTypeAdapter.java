package de.qabel.core.config;

import java.io.IOException;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblEcKeyPairTypeAdapter;

public class IdentitiesTypeAdapter extends TypeAdapter<Identities> {

	@Override
	public void write(JsonWriter out, Identities value) throws IOException {
		out.beginArray();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(QblECKeyPair.class, new QblEcKeyPairTypeAdapter());
		Gson gson = builder.create();
		Set<Identity> set = value.getIdentities();
		TypeAdapter<Identity> adapter = gson.getAdapter(Identity.class);
		for(Identity identity : set) {
			adapter.write(out, identity);
		}
		out.endArray();
	}

	@Override
	public Identities read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(QblECKeyPair.class, new QblEcKeyPairTypeAdapter());
		Gson gson = builder.create();		Identities identities = new Identities();
		TypeAdapter<Identity> adapter = gson.getAdapter(Identity.class);
		Identity identity = null; 
		
		in.beginArray();
		while(in.hasNext()) {
			identity = adapter.read(in);
			identities.put(identity);
		}
		in.endArray();
		
		return identities;
	}
}
