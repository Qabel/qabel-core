package de.qabel.core.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

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
		
		QblEncPublicKey encPublicKey = value.getEncPublicKey();
		out.name("public_enc_key");
		out.beginObject();
		out.name("modulus");
		out.value(encPublicKey.getModulus().toString());
		out.name("exponent");
		out.value(encPublicKey.getPublicExponent().toString());
		out.name("signature");
		out.value(getStringFromByteArray(encPublicKey.getPrimaryKeySignature()));
		out.endObject();
		
		QblSignPublicKey signPublicKey = value.getSignPublicKey();
		out.name("public_sign_key");
		out.beginObject();
		out.name("modulus");
		out.value(signPublicKey.getModulus().toString());
		out.name("exponent");
		out.value(signPublicKey.getPublicExponent().toString());
		out.name("signature");
		out.value(getStringFromByteArray(signPublicKey.getPrimaryKeySignature()));		
		out.endObject();
		
		out.endObject();		
		
		return;
	}

	@Override
	public QblPrimaryPublicKey read(JsonReader in) throws IOException {
		QblPrimaryPublicKey primaryPublicKey = null;
		QblEncPublicKey encPublicKey = null;
		QblSignPublicKey signPublicKey = null;
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
			case "public_enc_key":
				in.beginObject();
				encPublicKey = readEncPublicKey(in);
				in.endObject();
				break;
			case "public_sign_key":
				in.beginObject();
				signPublicKey = readSignPublicKey(in);
				in.endObject();
				break;				
			}
		}
		in.endObject();
		if(!(primaryPublicKey == null || encPublicKey == null || signPublicKey == null)) {
			try {
				primaryPublicKey.attachEncPublicKey(encPublicKey);
				primaryPublicKey.attachSignPublicKey(signPublicKey);
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