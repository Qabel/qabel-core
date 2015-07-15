package de.qabel.core.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.crypto.QblEcPublicKeyTypeAdapter;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

public class ContactTypeAdapter extends TypeAdapter<Contact> {

	@Override
	public void write(JsonWriter out, Contact value) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		out.beginObject();
		out.name("keys");
		builder.registerTypeAdapter(QblECPublicKey.class, new QblEcPublicKeyTypeAdapter());
		Gson gson = builder.create();
		TypeAdapter<QblECPublicKey> primaryKeyAdapter = gson.getAdapter(QblECPublicKey.class);
		primaryKeyAdapter.write(out, value.getEcPublicKey());

		out.name("alias");
		out.value(value.getAlias());

		out.name("my_identity");
		out.value(value.getContactOwnerKeyId());
		
		out.name("drop_urls");
		out.beginArray();
		Collection<DropURL> dropUrls = value.getDropUrls();
		TypeAdapter<URI> urlAdapter = gson.getAdapter(URI.class);
		for(DropURL dropUrl : dropUrls) {
			urlAdapter.write(out, dropUrl.getUri());
		}
		out.endArray();
		
		out.name("module_data");
		out.beginObject();
		//TODO: write module data
		out.endObject();

		// SyncSettingItem properties
		out.name("id").value(value.getId());
		out.name("created").value(value.getCreated());
		out.name("updated").value(value.getUpdated());
		out.name("deleted").value(value.getDeleted());

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
		String alias = null;
		QblECPublicKey ecPublicKey = null;
		Collection<DropURL> dropURLs = null;
		SyncSettingItem syncItem = new SyncSettingItem();
		in.beginObject();
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "keys":
				QblEcPublicKeyTypeAdapter publicKeyTypeAdapter = new QblEcPublicKeyTypeAdapter();
				ecPublicKey = publicKeyTypeAdapter.read(in);
				break;
			case "my_identity":
				contactOwnerKeyId = in.nextString();
				break;
			case "alias":
				alias = in.nextString();
				break;
			case "drop_urls":
				in.beginArray();
				dropURLs = new ArrayList<DropURL>();
				while(in.hasNext()) {
					try {
						dropURLs.add(new DropURL(in.nextString()));
					} catch (QblDropInvalidURL | URISyntaxException e) {
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
			// SyncSettingItem properties
			case "id":
				syncItem.setId(in.nextInt());
				break;
			case "created":
				syncItem.setCreated(in.nextLong());
				break;
			case "updated":
				syncItem.setUpdated(in.nextLong());
				break;
			case "deleted":
				syncItem.setDeleted(in.nextLong());
				break;
			}
		}
		in.endObject();
		
		if(ecPublicKey == null || contactOwnerKeyId == null || dropURLs == null) {
			return null;
		}
		
		contact = new Contact(contactOwnerKeyId, alias, dropURLs, ecPublicKey);

		// copy all sync item properties
		contact.setId(syncItem.getId());
		contact.setCreated(syncItem.getCreated());
		contact.setUpdated(syncItem.getUpdated());
		contact.setDeleted(syncItem.getDeleted());
		
		return contact;
	}
}
