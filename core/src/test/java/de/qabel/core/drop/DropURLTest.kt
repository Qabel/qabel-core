package de.qabel.core.drop

import de.qabel.core.config.DropServer
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import java.net.URI
import java.net.URISyntaxException

class DropURLTest {
    @Test
    @Throws(URISyntaxException::class, QblDropInvalidURL::class)
    fun validURLTest() {
        DropURL("http://www.foo.org/1234567890123456789012345678901234567890123")
    }

    @get:Rule
    var exception = ExpectedException.none()

    @Test
    @Throws(URISyntaxException::class, QblDropInvalidURL::class)
    fun tooShortURLTest() {
        exception.expect(QblDropInvalidURL::class.java)
        DropURL("http://www.bar.org/not43base64chars")
    }

    @Test
    @Throws(QblDropInvalidURL::class, URISyntaxException::class)
    fun tooLongURLTest() {
        exception.expect(QblDropInvalidURL::class.java)
        DropURL("http://www.foo.org/01234567890123456789012345678901234567890123")
    }

    @Test
    @Throws(URISyntaxException::class, QblDropInvalidURL::class)
    fun nonBase64URLTest() {
        exception.expect(QblDropInvalidURL::class.java)
        DropURL("http://www.baz.org/2@34567890123456789012345678901234567890123")
    }

    @Test
    @Throws(URISyntaxException::class, QblDropInvalidURL::class)
    fun testGeneration() {
        val server = DropServer()
        server.uri = URI("http://example.com")
        val drop = DropURL(server)
        DropURL(drop.toString())
    }
}
