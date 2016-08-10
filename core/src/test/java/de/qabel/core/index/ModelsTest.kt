package de.qabel.core.index

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import org.junit.Test

import org.junit.Assert.*

class ModelsTest {
    @Test
    fun testIdentityDataFromIdentity() {
        val helloDrop = DropURL("http://www.foo.org/1234567890123456789012345678901234567890123")
        val keyPair = QblECKeyPair()
        val identity = Identity("alias", listOf(helloDrop), keyPair)

        val identityData = UpdateIdentity.fromIdentity(identity, UpdateAction.CREATE)
        assertEquals(identityData.alias, "alias")
        assertEquals(identityData.keyPair, keyPair)
        assertEquals(identityData.dropURL, helloDrop)
    }

    @Test
    fun testUpdateItemSerialization() {
        val gson = createGson()
        val updateItem = UpdateField(
            action = UpdateAction.CREATE,
            field = FieldType.EMAIL,
            value = "foo@example.net"
        )

        val json = gson.toJson(updateItem)
        assertEquals("""{"action":"create","field":"email","value":"foo@example.net"}""", json)
    }
}
