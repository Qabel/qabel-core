package de.qabel.core.crypto;

import static org.junit.Assert.*;

import java.security.InvalidKeyException;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class QblKeySerializationTest {

	@Test
	public void qblPrimaryKeyPairTest() {
		QblKeyFactory kf = QblKeyFactory.getInstance();
		QblPrimaryKeyPair qpkp = kf.generateQblPrimaryKeyPair();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(QblPrimaryKeyPair.class, new QblPrimaryKeyPairTypeAdapter());
		Gson gson = builder.setPrettyPrinting().create();
		System.out.println("Serialized key: " + gson.toJson(qpkp));
		QblPrimaryKeyPair deserializedQpkp = gson.fromJson(gson.toJson(qpkp), QblPrimaryKeyPair.class);
		System.out.println("Deserialized key: " + gson.toJson(deserializedQpkp));
		
		assertEquals(qpkp, deserializedQpkp);
	}
	
	@Test
	public void qblPrimaryPublicKeyTest() throws InvalidKeyException {
		QblKeyFactory kf = QblKeyFactory.getInstance();
		QblPrimaryKeyPair qpkp = kf.generateQblPrimaryKeyPair();
		QblPrimaryPublicKey qppk = qpkp.getQblPrimaryPublicKey();
		qppk.attachEncPublicKey(qpkp.getQblEncPublicKey());
		qppk.attachSignPublicKey(qpkp.getQblSignPublicKey());
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(QblPrimaryPublicKey.class, new QblPrimaryPublicKeyTypeAdapter());
		Gson gson = builder.setPrettyPrinting().create();
		System.out.println("Serialized key: " + gson.toJson(qppk));
		QblPrimaryPublicKey deserializedQppk = gson.fromJson(gson.toJson(qppk), QblPrimaryPublicKey.class);
		System.out.println("Deserialized key: " + gson.toJson(deserializedQppk));
		
		assertEquals(qppk, deserializedQppk);
	}
}
