package de.qabel.box.storage.hash

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class QabelBoxDigestProviderTest {
    @After
    fun resetStatics() {
        QabelBoxDigestProvider.randomizeName = false
    }

    @Test
    fun randomizesNameIfRequired() {
        QabelBoxDigestProvider.randomizeName = true
        val instance1 = QabelBoxDigestProvider()
        val instance2 = QabelBoxDigestProvider()

        assertNotEquals(instance2.name, instance1.name)
    }

    @Test
    fun unrandomizedName() {
        assertEquals("Qabel", QabelBoxDigestProvider().name)
    }
}
