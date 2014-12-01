package de.qabel.core.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

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
		
		List<QblEncKeyPair> encKeyPairs = value.getEncKeyPairs();
		out.name("enc_keys");
		out.beginArray();
		QblEncPublicKey qepk;
		for(QblEncKeyPair pair : encKeyPairs) {
			qepk = pair.getQblEncPublicKey();
			out.beginObject();
			out.name("modulus");
			out.value(qepk.getModulus().toString());
			out.name("public_exponent");
			out.value(qepk.getPublicExponent().toString());
			out.name("private_exponent");
			out.value(pair.getRSAPrivateKey().getPrivateExponent().toString());
			out.endObject();
		}
		out.endArray();
		
		List<QblSignKeyPair> signKeyPairs = value.getSignKeyPairs();
		out.name("sign_keys");
		out.beginArray();
		QblSignPublicKey qspk;
		for(QblSignKeyPair pair : signKeyPairs) {
			qspk = pair.getQblSignPublicKey();
			out.beginObject();
			out.name("modulus");
			out.value(qspk.getModulus().toString());
			out.name("public_exponent");
			out.value(qspk.getPublicExponent().toString());
			out.name("private_exponent");
			out.value(pair.getRSAPrivateKey().getPrivateExponent().toString());
			out.endObject();
		}
		out.endArray();
		
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
		List<QblEncKeyPair> encKeyPairs = null;
		List<QblSignKeyPair> signKeyPairs = null;
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
			case "enc_keys":
				encKeyPairs = new ArrayList<QblEncKeyPair>();
				in.beginArray();
				while(in.hasNext()) {
					in.beginObject();
					encKeyPairs.add(readEncKeyPair(in));
					in.endObject();
				}
				in.endArray();
				break;
			case "sign_keys":
				signKeyPairs = new ArrayList<QblSignKeyPair>();
				in.beginArray();
				while(in.hasNext()) {
					in.beginObject();
					signKeyPairs.add(readSignKeyPair(in));
					in.endObject();
				}
				in.endArray();
				break;
			}
		}
		in.endObject();
		
		if (primaryKeyPair == null || encKeyPairs == null || signKeyPairs == null) {
			return null;
		}
		
		for(QblEncKeyPair pair : encKeyPairs) {
			primaryKeyPair.attachEncKeyPair(pair);
		}
		for(QblSignKeyPair pair : signKeyPairs) {
			primaryKeyPair.attachSignKeyPair(pair);
		}
		
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

