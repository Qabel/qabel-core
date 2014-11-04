package de.qabel.core.config;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import de.qabel.core.crypto.QblPrimaryPublicKey;
import de.qabel.core.crypto.QblPrimaryPublicKeyTypeAdapter;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

public class ContactTypeAdapter extends TypeAdapter<Contact> {

	@Override
	public void write(JsonWriter out, Contact value) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		out.beginObject();
		out.name("keys");
		builder.registerTypeAdapter(QblPrimaryPublicKey.class, new QblPrimaryPublicKeyTypeAdapter());
		Gson gson = builder.create();
		TypeAdapter<QblPrimaryPublicKey> primaryKeyAdapter = gson.getAdapter(QblPrimaryPublicKey.class);
		primaryKeyAdapter.write(out, value.getPrimaryPublicKey());
		
		out.name("my_identity");
		out.value(value.getContactOwnerKeyId());
		
		out.name("drop_urls");
		out.beginArray();
		Collection<DropURL> dropUrls = value.getDropUrls();
		TypeAdapter<URL> urlAdapter = gson.getAdapter(URL.class);
		for(DropURL url : dropUrls) {
			urlAdapter.write(out, url.getUrl());
		}
		out.endArray();
		
		out.name("module_data");
		out.beginObject();
		out.endObject();
		out.endObject();
		
		return;
	}

	@Override
	public Contact read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		Contact contact;
		String contactOwnerKeyId = null;
		QblPrimaryPublicKey qppk = null;
		Collection<DropURL> dropURLs = null;
		in.beginObject();
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "keys":
				QblPrimaryPublicKeyTypeAdapter publicKeyTypeAdapter = new QblPrimaryPublicKeyTypeAdapter();
				qppk = publicKeyTypeAdapter.read(in);
				break;
			case "my_identity":
				contactOwnerKeyId = in.nextString();
				break;
			case "drop_urls":
				in.beginArray();
				dropURLs = new ArrayList<DropURL>();
				while(in.hasNext()) {
					try {
						dropURLs.add(new DropURL(in.nextString()));
					} catch (QblDropInvalidURL e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				in.endArray();
				break;
			case "module_data":
				in.beginObject();
				//TODO: read module data
				in.endObject();
				break;
			}
		}
		in.endObject();
		
		if(qppk == null || contactOwnerKeyId == null || dropURLs == null) {
			return null;
		}
		
		contact = new Contact(contactOwnerKeyId);
		contact.setPrimaryPublicKey(qppk);
		contact.getDropUrls().addAll(dropURLs);
		
		return contact;
	}
}