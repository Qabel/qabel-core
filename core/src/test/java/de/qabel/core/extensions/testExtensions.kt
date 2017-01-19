package de.qabel.core.extensions

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.should.shouldMatch
import de.qabel.core.TestServer
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import kotlin.reflect.KClass

interface CoreTestCase {
    val dropGenerator: DropUrlGenerator get() = DropUrlGenerator(TestServer.DROP)
}

fun CoreTestCase.createIdentity(alias: String,
                                dropURL: DropURL = dropGenerator.generateUrl(),
                                keyPair: QblECKeyPair = QblECKeyPair()) =
    Identity(alias, mutableListOf(dropURL), keyPair).apply { prefixes.add(Prefix("prefix")) }

fun CoreTestCase.createContact(alias: String,
                               dropURL: DropURL = dropGenerator.generateUrl(),
                               publicKey: QblECPublicKey = QblECKeyPair().pub) =
    Contact(alias, mutableListOf(dropURL), publicKey)

fun CoreTestCase.copy(identity : Identity) : Identity =
    createIdentity(identity.alias, identity.helloDropUrl, identity.primaryKeyPair).apply {
        id = identity.id
        email = identity.email
        emailStatus = identity.emailStatus
        phone = identity.phone
        phoneStatus = identity.phoneStatus
        prefixes = identity.prefixes
    }

fun CoreTestCase.randomFile(size: Long): File =
    Files.createTempFile("qabel_", ".tmp").apply {
        IOUtils.write(CryptoUtils().getRandomBytes(size.toInt()), FileOutputStream(this.toFile()))
    }.toFile()


inline fun <reified T : Throwable> assertThrows(expectedException: KClass<T>, operation: () -> Any?): T {
    try {
        operation()
    } catch (ex: Throwable) {
        assertEquals(expectedException.java, ex.javaClass)
        return ex as T
    }
    fail("Expected exception ${expectedException.simpleName} not thrown.")
    throw IllegalStateException("fail()")  /* Kotlin doesn't know that fail() always throws */
}

infix fun <T> T.shouldNotMatch(matcher: Matcher<T>) = this shouldMatch Matcher.Negation<T>(matcher)
