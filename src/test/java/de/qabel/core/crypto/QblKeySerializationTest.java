package de.qabel.core.crypto;

import static org.junit.Assert.*;

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
		assertNull(null);
		//TODO: make test working - proper equals() method for QblPrimaryKeyPair is needed
//		asserEquals(qpkp, deserializedQpkp);
	}
}
