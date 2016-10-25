package de.qabel.core.config

import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import org.apache.commons.lang3.SerializationUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class IdentitySerializeTest() : CoreTestCase {

    @Test
    fun testSerializeIdentity() {
        val identity = createIdentity("TestIdentity")
        val bytes = SerializationUtils.serialize(identity)
        val receivedIdentity = SerializationUtils.deserialize<Identity>(bytes)
        assertEquals(identity, receivedIdentity)
    }

}
