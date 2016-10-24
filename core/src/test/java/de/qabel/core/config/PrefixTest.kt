package de.qabel.core.config

import de.qabel.core.config.Prefix.TYPE.CLIENT
import de.qabel.core.config.Prefix.TYPE.USER
import org.junit.Assert.assertEquals
import org.junit.Test

class PrefixTest {
    @Test
    fun fromString() {
        assertEquals(USER, Prefix.TYPE.valueOf("USER"))
        assertEquals(CLIENT, Prefix.TYPE.valueOf("CLIENT"))
    }

    @Test
    fun testToString() {
        assertEquals("USER", USER.toString())
        assertEquals("CLIENT", CLIENT.toString())
    }
}
