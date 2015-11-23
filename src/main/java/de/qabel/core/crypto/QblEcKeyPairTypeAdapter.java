package de.qabel.core.crypto;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.spongycastle.util.encoders.Hex;

public class QblEcKeyPairTypeAdapter extends TypeAdapter<QblECKeyPair> {

	@Override
	public void write(JsonWriter out, QblECKeyPair value) throws IOException {
		out.beginObject();
		out.name("private_key");
		out.value(Hex.toHexString(value.getPrivateKey()));
		out.endObject();
	}

	@Override
	public QblECKeyPair read(JsonReader in) throws IOException {
		QblECKeyPair ecKeyPair = null;

		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		in.beginObject();
		if (in.hasNext() && in.nextName().equals("private_key")) {
			ecKeyPair = new QblECKeyPair(Hex.decode(in.nextString()));
		}
		in.endObject();
		return ecKeyPair;
	}
}

