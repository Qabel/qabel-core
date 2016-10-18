package de.qabel.box.storage

import de.qabel.core.config.Prefix.TYPE.CLIENT
import de.qabel.core.config.Prefix.TYPE.USER
import de.qabel.core.crypto.QblECKeyPair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RootRefCalculatorTest {
    val privateKey = QblECKeyPair().privateKey
    val calculator = RootRefCalculator()

    @Test
    fun calculatesOriginalRootRefForUserType() {
        val rootRef = calculator.rootFor(privateKey, USER, "/myPrefix")

        assertEquals(BoxVolumeTest.originalRootRef("/myPrefix", privateKey), rootRef)
    }

    @Test
    fun calculatesAnotherRootRefForTypeClient() {
        val userRootRef = calculator.rootFor(privateKey, USER, "prefix")
        val clientRootRef = calculator.rootFor(privateKey, CLIENT, "prefix")

        assertNotEquals(userRootRef, clientRootRef)
    }

    @Test
    fun calculatesAnotherRootRefForAnotherPrefix() {
        val clientRootRefA = calculator.rootFor(privateKey, CLIENT, "a")
        val clientRootRefB = calculator.rootFor(privateKey, CLIENT, "b")

        assertNotEquals(clientRootRefA, clientRootRefB)
    }

    @Test
    fun calculatesConstantClientPrefixes() = assertEquals(
        calculator.rootFor(privateKey, CLIENT, "a"),
        calculator.rootFor(privateKey, CLIENT, "a")
    )
}
