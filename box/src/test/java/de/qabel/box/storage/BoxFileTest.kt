package de.qabel.box.storage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoxFileTest {
    val someFile = BoxFile("prefix", "block", "name", 123, 456, ByteArray(0))

    @Test
    fun knowsWhenItDoesntHaveAHashYet() = assertTrue(!someFile.isHashed())

    @Test
    fun knowsWhenItHasAHash() {
        someFile.setHash("hash".toByteArray(), "myalgo")
        assertTrue(someFile.isHashed())
    }

    @Test
    fun knowsWhenItIsntShared() = assertTrue(!someFile.isShared())

    @Test
    fun knowsWhenItIsShared() {
        someFile.shared = Share.create("a", "b".toByteArray())
        assertTrue(someFile.isShared())
    }
}
