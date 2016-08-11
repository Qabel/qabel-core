package de.qabel.core.http

import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropInvalidURL
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.util.*

class MainDropServerTest {

    var postedAt: Long = 0
    private lateinit var workingUri: URI
    private lateinit var tooShortUri: URI
    private lateinit var notExistingUri: URI
    private lateinit var shouldContainMessagesUri: URI
    private lateinit var shouldContainNoNewMessagesSinceDateUri: URI

    private lateinit var dropServer: DropServerHttp

    @Before
    fun setUp() {
        val dropGenerator = DropUrlGenerator("http://localhost:5000")
        workingUri = dropGenerator.generateUrl().uri
        tooShortUri = URI("http://localhost:5000/IAmTooShort")
        notExistingUri = dropGenerator.generateUrl().uri
        shouldContainMessagesUri = dropGenerator.generateUrl().uri
        shouldContainNoNewMessagesSinceDateUri = dropGenerator.generateUrl().uri

        //prepare dropserver content for tests.
        dropServer = MainDropServer()
        dropServer.sendBytes(shouldContainMessagesUri, "shouldContainMessagesTestMessage".toByteArray())
        dropServer.sendBytes(shouldContainNoNewMessagesSinceDateUri, "shouldContainNoNewMessagesSinceDate".toByteArray())
        postedAt = System.currentTimeMillis()
    }

    // POST 200
    @Test
    fun sendMessage() {
        val message = "Test".toByteArray()
        dropServer.sendBytes(workingUri, message)
        val result = dropServer.receiveMessageBytes(workingUri, "")
        assertThat(result.third.find { it.arrayEquals(message) }, notNullValue())
    }

    @Test
    fun testByteArrayEquals() {
        val a = "TEST".toByteArray()
        val b = "TEST".toByteArray()
        assertTrue(a.arrayEquals(b))
        assertFalse(a.equals(b))
    }

    fun ByteArray.arrayEquals(other: ByteArray): Boolean {
        if (size != other.size) {
            return false
        }
        for (i in 0 until size) {
            if (this[i] != other[i]) {
                return false
            }
        }
        return true
    }

    // POST 400
    @Test(expected = QblDropInvalidURL::class)
    fun postMessageNotGivenOrInvalid() {
        dropServer.sendBytes(workingUri, "".toByteArray())
    }

    // POST 413
    @Test(expected = QblDropInvalidMessageSizeException::class)
    fun testMessageToBig() {
        val chars = CharArray(2574) // one byte more than the server accepts
        Arrays.fill(chars, 'a')
        dropServer.sendBytes(workingUri,
            String(chars).toByteArray())
    }

    // GET 200
    @Test
    fun testCompleteDrop() {
        val result = dropServer.receiveMessageBytes(shouldContainMessagesUri, "")
        assertThat(result.first, equalTo(200))
        assertNotEquals(result.second, "")
    }

    // GET 400
    @Test(expected = QblDropInvalidURL::class)
    fun testShortInvalidURI() {
        dropServer.receiveMessageBytes(tooShortUri, "")
    }

    // GET 204
    @Test
    fun testEmptyDrop() {
        val result = dropServer.receiveMessageBytes(notExistingUri, "")
        assertThat(result.third, hasSize(0))
        assertEquals(204, result.first)
    }

    // GET 200 SINCE
    @Test
    @Throws(Exception::class)
    fun testReceiveMessage() {
        // When
        val result = dropServer.receiveMessageBytes(shouldContainMessagesUri, "")
        assertEquals(200, result.first)
        assertTrue(result.third.size > 0)
    }

}
