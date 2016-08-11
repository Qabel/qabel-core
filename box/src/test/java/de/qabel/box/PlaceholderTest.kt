package de.qabel.box

import de.qabel.core.crypto.CryptoUtils
import org.junit.Test
import org.spongycastle.crypto.params.KeyParameter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class PlaceholderTest {
    @Test
    fun compileCheckTest() {
        assertTrue(Placeholder() is Placeholder)
    }

    @Test
    @Throws(Exception::class)
    fun dependencyTest() {
        val utils = CryptoUtils()
        val message = "testmessage"
        val key = utils.generateSymmetricKey()
        val encryptedMessage = utils.encrypt(key, "?".toByteArray(), message.toByteArray(), "?".toByteArray())
        val decryptedMessage = String(utils.decrypt(key, "?".toByteArray(), encryptedMessage, "?".toByteArray()))
        assertEquals(decryptedMessage, message)
    }
}
