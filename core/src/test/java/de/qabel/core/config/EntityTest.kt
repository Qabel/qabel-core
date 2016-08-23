package de.qabel.core.config

import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import org.junit.Assert.*
import org.hamcrest.Matchers.*
import org.junit.Test
import org.spongycastle.util.encoders.Hex
import java.util.*


class EntityTest {

    val dropUrl = DropUrlGenerator("http://localhost").generateUrl().toString()
    val key = Hex.toHexString(QblECKeyPair().privateKey)
    val identityA = Identity("ident", listOf(DropURL(dropUrl)), QblECKeyPair(Hex.decode(key)))
    val identityB = Identity("ident", listOf(DropURL(dropUrl)), QblECKeyPair(Hex.decode(key)))
    val contactA = Contact("contact", listOf(DropURL(dropUrl)), QblECPublicKey("testKey".toByteArray()))
    val contactB = Contact("contact", listOf(DropURL(dropUrl)), QblECPublicKey("testKey".toByteArray()))

    @Test
    fun testIdentity() {
        assertEquals(identityA.hashCode(), identityB.hashCode())
        assertEquals(identityA, identityB)
    }

    @Test
    fun testContact() {
        assertEquals(contactA.hashCode(), contactB.hashCode())
        assertEquals(contactA, contactB)
        assertNotEquals(identityA, contactA)
    }

    @Test
    fun testMap() {
        val map = HashMap<Contact, String>()
        map.put(contactA, contactA.keyIdentifier)
        map.put(contactB, contactB.keyIdentifier)
        assertThat(map.keys, hasSize(1))
        assertThat(map[contactB], equalTo(map[contactA]))
    }
}
