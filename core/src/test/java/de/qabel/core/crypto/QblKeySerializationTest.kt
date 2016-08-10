package de.qabel.core.crypto

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.crypto.QblEcKeyPairTypeAdapter
import de.qabel.core.crypto.QblEcPublicKeyTypeAdapter
import org.junit.Test

import java.security.InvalidKeyException

import org.junit.Assert.assertEquals

class QblKeySerializationTest {

    @Test
    fun qblPrimaryKeyPairTest() {
        val ecKeyPair = QblECKeyPair()
        val builder = GsonBuilder()
        builder.registerTypeAdapter(QblECKeyPair::class.java, QblEcKeyPairTypeAdapter())
        val gson = builder.setPrettyPrinting().create()
        println("Serialized key: " + gson.toJson(ecKeyPair))
        val deserializedEcKeyPair = gson.fromJson(gson.toJson(ecKeyPair), QblECKeyPair::class.java)
        println("Deserialized key: " + gson.toJson(deserializedEcKeyPair))

        assertEquals(ecKeyPair, deserializedEcKeyPair)
    }

    @Test
    @Throws(InvalidKeyException::class)
    fun qblPrimaryPublicKeyTest() {
        val ecKeyPair = QblECKeyPair()
        val ecPublicKey = ecKeyPair.pub
        val builder = GsonBuilder()
        builder.registerTypeAdapter(QblECPublicKey::class.java, QblEcPublicKeyTypeAdapter())
        val gson = builder.setPrettyPrinting().create()
        println("Serialized key: " + gson.toJson(ecPublicKey))
        val deserializedQppk = gson.fromJson(gson.toJson(ecPublicKey), QblECPublicKey::class.java)
        println("Deserialized key: " + gson.toJson(deserializedQppk))

        assertEquals(ecPublicKey, deserializedQppk)
    }
}
