package de.qabel.core.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class QblPrimaryPublicKeyTypeAdapter extends TypeAdapter<QblPrimaryPublicKey> {

	private final static Logger logger = LogManager.getLogger(QblPrimaryPublicKeyTypeAdapter.class
			.getName());

	
	@Override
	public void write(JsonWriter out, QblPrimaryPublicKey value) throws IOException {
		out.beginObject();
		out.name("public_primary_key");
		out.beginObject();
		out.name("modulus");
		out.value(value.getModulus().toString());
		out.name("exponent");
		out.value(value.getPublicExponent().toString());
		out.endObject();
		
		List<QblEncPublicKey> encPublicKeys = value.getEncPublicKeys();
		out.name("public_enc_keys");
		out.beginArray();
		for(QblEncPublicKey key : encPublicKeys) {
			out.beginObject();
			out.name("modulus");
			out.value(key.getModulus().toString());
			out.name("exponent");
			out.value(key.getPublicExponent().toString());
			out.name("signature");
			out.value(getStringFromByteArray(key.getPrimaryKeySignature()));
			out.endObject();
		}
		out.endArray();
		
		List<QblSignPublicKey> signPublicKeys = value.getSignPublicKeys();
		out.name("public_sign_keys");
		out.beginArray();
		for(QblSignPublicKey key : signPublicKeys) {
			out.beginObject();
			out.name("modulus");
			out.value(key.getModulus().toString());
			out.name("exponent");
			out.value(key.getPublicExponent().toString());
			out.name("signature");
			out.value(getStringFromByteArray(key.getPrimaryKeySignature()));		
			out.endObject();
		}
		out.endArray();
		
		out.endObject();		
		
		return;
	}

	@Override
	public QblPrimaryPublicKey read(JsonReader in) throws IOException {
		QblPrimaryPublicKey primaryPublicKey = null;
		List<QblEncPublicKey> encPublicKeys = null;
		List<QblSignPublicKey> signPublicKeys = null;
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		in.beginObject();
		while (in.hasNext()) {
			switch(in.nextName()){
			case "public_primary_key":
				in.beginObject();
				primaryPublicKey = readPrimaryPublicKey(in);
				in.endObject();
				break;
			case "public_enc_keys":
				encPublicKeys = new ArrayList<QblEncPublicKey>();
				in.beginArray();
				while(in.hasNext()) {
					in.beginObject();
					encPublicKeys.add(readEncPublicKey(in));
					in.endObject();
				}
				in.endArray();
				break;
			case "public_sign_keys":
				signPublicKeys = new ArrayList<QblSignPublicKey>();
				in.beginArray();
				while(in.hasNext()) {
					in.beginObject();
					signPublicKeys.add(readSignPublicKey(in));
					in.endObject();
				}
				in.endArray();
				break;				
			}
		}
		in.endObject();
		if(!(primaryPublicKey == null || encPublicKeys == null || signPublicKeys == null)) {
			try {
				for(QblEncPublicKey key : encPublicKeys) {
					primaryPublicKey.attachEncPublicKey(key);
				}
				for(QblSignPublicKey key : signPublicKeys) {
					primaryPublicKey.attachSignPublicKey(key);
				}
			} catch (InvalidKeyException e) {
				logger.error("Read public key is invalid!");			
			}
		}
		
		return primaryPublicKey;
	}
	
	private QblPrimaryPublicKey readPrimaryPublicKey(JsonReader in) throws IOException {
		QblPrimaryPublicKey primaryPublicKey = null;
		String modulus = null;
		String exponent = null;
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "modulus":
				modulus = in.nextString();
				break;
			case "exponent":
				exponent = in.nextString();
				break;
			}
		}
		
		if(modulus == null || exponent == null) {
			return null;
		}

		KeyFactory kf;
		RSAPublicKey rsaPubKey = null;
		try {
			kf = KeyFactory.getInstance("RSA");
			rsaPubKey = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent)));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		primaryPublicKey = new QblPrimaryPublicKey(rsaPubKey);
		return primaryPublicKey;
	}
	
	private QblEncPublicKey readEncPublicKey(JsonReader in) throws IOException {
		QblEncPublicKey encPublicKey = null;
		String modulus = null;
		String exponent = null;
		String signature = null;
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "modulus":
				modulus = in.nextString();
				break;
			case "exponent":
				exponent = in.nextString();
				break;
			case "signature":
				signature = in.nextString();
			}
		}
		
		if(modulus == null || exponent == null || signature == null) {
			return null;
		}
		
		KeyFactory kf;
		RSAPublicKey rsaPubKey = null;
		try {
			kf = KeyFactory.getInstance("RSA");
			rsaPubKey = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent)));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		encPublicKey = new QblEncPublicKey(rsaPubKey);
		encPublicKey.setPrimaryKeySignature(getByteArrayFromString(signature));
		return encPublicKey;
	}
	
	private QblSignPublicKey readSignPublicKey(JsonReader in) throws IOException {
		QblSignPublicKey signPublicKey = null;
		String modulus = null;
		String exponent = null;
		String signature = null;
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "modulus":
				modulus = in.nextString();
				break;
			case "exponent":
				exponent = in.nextString();
				break;
			case "signature":
				signature = in.nextString();
			}
		}
		
		if(modulus == null || exponent == null || signature == null) {
			return null;
		}
		
		KeyFactory kf;
		RSAPublicKey rsaPubKey = null;
		try {
			kf = KeyFactory.getInstance("RSA");
			rsaPubKey = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(exponent)));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		signPublicKey = new QblSignPublicKey(rsaPubKey);
		signPublicKey.setPrimaryKeySignature(getByteArrayFromString(signature));
		return signPublicKey;
	}
	
	private String getStringFromByteArray(byte[] bytes) {
		String hex = DatatypeConverter.printHexBinary(bytes);
		return hex;
	}
	
	private byte[] getByteArrayFromString(String str) {
		byte[] bytes = DatatypeConverter.parseHexBinary(str);
		return bytes;
	}
}