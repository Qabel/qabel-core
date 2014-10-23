package de.qabel.core.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class QblPrimaryKeyPairTypeAdapter extends TypeAdapter<QblPrimaryKeyPair> {

	@Override
	public void write(JsonWriter out, QblPrimaryKeyPair value) throws IOException {
		out.beginObject();
		
		QblPrimaryPublicKey primaryPublicKey = value.getQblPrimaryPublicKey();		
		out.name("primary_key");
		out.beginObject();
		out.name("modulus");
		out.value(primaryPublicKey.getModulus().toString());
		out.name("public_exponent");
		out.value(primaryPublicKey.getPublicExponent().toString());
		out.name("private_exponent");
		out.value(value.getRSAPrivateKey().getPrivateExponent().toString());
		out.endObject();
		
		QblEncPublicKey encPublicKey = value.getQblEncPublicKey();
		out.name("enc_key");
		out.beginObject();
		out.name("modulus");
		out.value(encPublicKey.getModulus().toString());
		out.name("public_exponent");
		out.value(encPublicKey.getPublicExponent().toString());
		out.name("private_exponent");
		out.value(value.getQblEncPrivateKey().getPrivateExponent().toString());
		out.endObject();
		
		QblSignPublicKey signPublicKey = value.getQblSignPublicKey();
		out.name("sign_key");
		out.beginObject();
		out.name("modulus");
		out.value(signPublicKey.getModulus().toString());
		out.name("public_exponent");
		out.value(signPublicKey.getPublicExponent().toString());
		out.name("private_exponent");
		out.value(value.getQblSignPrivateKey().getPrivateExponent().toString());
		out.endObject();
		
		out.endObject();
		
		return;
	}

	@Override
	public QblPrimaryKeyPair read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		QblPrimaryKeyPair primaryKeyPair = null;
		QblEncKeyPair encKeyPair = null;
		QblSignKeyPair signKeyPair = null;
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		in.beginObject();
		while (in.hasNext()) {
			switch(in.nextName()){
			case "primary_key":
				in.beginObject();
				primaryKeyPair = readPrimaryKeyPair(in);
				in.endObject();
				break;
			case "enc_key":
				in.beginObject();
				encKeyPair = readEncKeyPair(in);
				in.endObject();
				break;
			case "sign_key":
				in.beginObject();
				signKeyPair = readSignKeyPair(in);
				in.endObject();
				break;
			}
		}
		in.endObject();
		
		if (primaryKeyPair == null || encKeyPair == null || signKeyPair == null) {
			return null;
		}
		
		primaryKeyPair.attachEncKeyPair(encKeyPair);
		primaryKeyPair.attachSignKeyPair(signKeyPair);
		
		return primaryKeyPair;
	}
	
	private QblPrimaryKeyPair readPrimaryKeyPair(JsonReader in) throws IOException {
		QblKeyFactory keyFactory = QblKeyFactory.getInstance();		
		String modulus = null;
		String publicExponent = null;
		String privateExponent = null;
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "modulus":
				modulus = in.nextString();
				break;
			case "public_exponent":
				publicExponent = in.nextString();
				break;
			case "private_exponent":
				privateExponent = in.nextString();
				break;
			}
		}
		
		if(modulus == null || publicExponent == null || privateExponent == null) {
			return null;
		}
		
		try {
			return keyFactory.createQblPrimaryKeyPair(new BigInteger(modulus), new BigInteger(privateExponent), new BigInteger(publicExponent));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private QblEncKeyPair readEncKeyPair(JsonReader in) throws IOException {
		QblKeyFactory keyFactory = QblKeyFactory.getInstance();
		String modulus = null;
		String publicExponent = null;
		String privateExponent = null;
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "modulus":
				modulus = in.nextString();
				break;
			case "public_exponent":
				publicExponent = in.nextString();
				break;
			case "private_exponent":
				privateExponent = in.nextString();
				break;
			}
		}
		
		if(modulus == null || publicExponent == null || privateExponent == null) {
			return null;
		}
		
		try {
			return keyFactory.createQblEncKeyPair(new BigInteger(modulus), new BigInteger(privateExponent), new BigInteger(publicExponent));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private QblSignKeyPair readSignKeyPair(JsonReader in) throws IOException {
		QblKeyFactory keyFactory = QblKeyFactory.getInstance();
		String modulus = null;
		String publicExponent = null;
		String privateExponent = null;
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "modulus":
				modulus = in.nextString();
				break;
			case "public_exponent":
				publicExponent = in.nextString();
				break;
			case "private_exponent":
				privateExponent = in.nextString();
			}
		}
		
		if(modulus == null || publicExponent == null || privateExponent == null) {
			return null;
		}
		
		try {
			return keyFactory.createQblSignKeyPair(new BigInteger(modulus), new BigInteger(privateExponent), new BigInteger(publicExponent));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

