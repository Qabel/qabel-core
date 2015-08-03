package de.qabel.core.crypto;

import java.io.IOException;

import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bouncycastle.util.encoders.Hex;

public class QblEcPublicKeyTypeAdapter extends TypeAdapter<QblECPublicKey> {

	private final static Logger logger = LoggerFactory.getLogger(QblEcPublicKeyTypeAdapter.class
			.getName());

	@Override
	public void write(JsonWriter out, QblECPublicKey value) throws IOException {
		out.beginObject();
		out.name("public_key");
		out.value(Hex.toHexString(value.getKey()));
		out.endObject();
	}

	@Override
	public QblECPublicKey read(JsonReader in) throws IOException {
		QblECPublicKey ecPublicKey = null;

		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		in.beginObject();
		if(in.hasNext() && in.nextName().equals("public_key")) {
			ecPublicKey = new QblECPublicKey(Hex.decode(in.nextString()));
		}
		in.endObject();
		return ecPublicKey;
	}
}