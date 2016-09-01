package de.qabel.core.extensions

import de.qabel.core.TestServer
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import kotlin.reflect.KClass

interface CoreTestCase {
    val dropGenerator: DropUrlGenerator get() = DropUrlGenerator(TestServer.DROP)
}

fun CoreTestCase.createIdentity(alias: String,
                                dropURL: DropURL = dropGenerator.generateUrl(),
                                keyPair: QblECKeyPair = QblECKeyPair()) =
    Identity(alias, mutableListOf(dropURL), keyPair)

fun CoreTestCase.createContact(alias: String,
                               dropURL: DropURL = dropGenerator.generateUrl(),
                               publicKey: QblECPublicKey = QblECPublicKey(RandomStringUtils.random(32).toByteArray())) =
    Contact(alias, mutableListOf(dropURL), publicKey)

fun <T : Throwable> assertThrows(expectedException: KClass<T>, operation: () -> Any?): T =
    try {
        operation()
        fail("Expected exception ${expectedException.simpleName} not thrown.")
        throw IllegalStateException("fail()")  /* Kotlin doesn't know that fail() always throws */
    } catch (ex: Throwable) {
        assertEquals(ex.javaClass, expectedException.java)
        ex as T
    }
