package de.qabel.core.crypto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.crypto.QblEcKeyPairTypeAdapter;
import de.qabel.core.crypto.QblEcPublicKeyTypeAdapter;
import org.junit.Test;

import java.security.InvalidKeyException;

import static org.junit.Assert.assertEquals;

public class QblKeySerializationTest {

    @Test
    public void qblPrimaryKeyPairTest() {
        QblECKeyPair ecKeyPair = new QblECKeyPair();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(QblECKeyPair.class, new QblEcKeyPairTypeAdapter());
        Gson gson = builder.setPrettyPrinting().create();
        System.out.println("Serialized key: " + gson.toJson(ecKeyPair));
        QblECKeyPair deserializedEcKeyPair = gson.fromJson(gson.toJson(ecKeyPair), QblECKeyPair.class);
        System.out.println("Deserialized key: " + gson.toJson(deserializedEcKeyPair));

        assertEquals(ecKeyPair, deserializedEcKeyPair);
    }

    @Test
    public void qblPrimaryPublicKeyTest() throws InvalidKeyException {
        QblECKeyPair ecKeyPair = new QblECKeyPair();
        QblECPublicKey ecPublicKey = ecKeyPair.getPub();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(QblECPublicKey.class, new QblEcPublicKeyTypeAdapter());
        Gson gson = builder.setPrettyPrinting().create();
        System.out.println("Serialized key: " + gson.toJson(ecPublicKey));
        QblECPublicKey deserializedQppk = gson.fromJson(gson.toJson(ecPublicKey), QblECPublicKey.class);
        System.out.println("Deserialized key: " + gson.toJson(deserializedQppk));

        assertEquals(ecPublicKey, deserializedQppk);
    }
}
