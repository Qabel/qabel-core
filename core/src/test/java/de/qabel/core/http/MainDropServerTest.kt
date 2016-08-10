package de.qabel.core.http

import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropInvalidURL
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
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
        workingUri = URI(
            "http://localhost:5000/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl")
        tooShortUri = URI("http://localhost:5000/IAmTooShort")
        notExistingUri = URI("http://localhost:5000/abcdefghijklmnopqrstuvwxyzabcnotExistingUrl")
        shouldContainMessagesUri = URI("http://localhost:5000/abcdefghijklmnopqrstuvshouldContainMessages")
        shouldContainNoNewMessagesSinceDateUri = URI("http://localhost:5000/xbcdefghshouldContainNoNewMessagesSinceDate")

        //prepare dropserver content for tests.
        dropServer = MainDropServer()
        dropServer.sendBytes(shouldContainMessagesUri, "shouldContainMessagesTestMessage".toByteArray())
        dropServer.sendBytes(shouldContainNoNewMessagesSinceDateUri, "shouldContainNoNewMessagesSinceDate".toByteArray())
    }

    // POST 200
    @Test
    @Throws(Exception::class)
    fun postMessageOk() {
        // Given
        val message = "Test"
        // When
        dropServer.sendBytes(workingUri, message.toByteArray())
        postedAt = System.currentTimeMillis()
        assertTrue(dropServer.receiveMessageBytes(workingUri, "").third.contains(message.toByteArray()))
    }

    // POST 400
    @Test(expected = QblDropInvalidURL::class)
    fun postMessageNotGivenOrInvalid() {
        dropServer.sendBytes(workingUri, "".toByteArray())
    }

    // POST 413
    @Test(expected = QblDropInvalidMessageSizeException::class)
    fun postMessageTooBig() {
        val chars = CharArray(2574) // one byte more than the server accepts
        Arrays.fill(chars, 'a')
        dropServer.sendBytes(workingUri,
            String(chars).toByteArray())
    }

    // GET 200
    @Test
    fun getRequestShouldGetCompleteDrop() {
        val result = dropServer.receiveMessageBytes(workingUri, "")
        assertThat(result.third, hasSize(0))
        assertThat(result.first, equalTo(200))
        assertNotEquals(result.second, "")
    }

    // GET 400
    @Test(expected = QblDropInvalidURL::class)
    fun getRequestWithInvalidOrMissingDropIdShouldBe400() {
        dropServer.receiveMessageBytes(tooShortUri, "")
    }

    // GET 204
    @Test
    @Throws(Exception::class)
    fun getRequestForEmptyDropShouldBe204() {
        val result = dropServer.receiveMessageBytes(notExistingUri, "")
        assertThat(result.third, hasSize(0))
        assertEquals(204, result.first)
    }

    // GET 200 SINCE
    @Test
    @Throws(Exception::class)
    fun getRequestShouldEntriesSinceDate() {
        // When
        val result = dropServer.receiveMessageBytes(shouldContainMessagesUri, "")
        println(result)
        // assertThat(result.third, has)
        assertEquals(200, result.first)
    }

    // GET 304 SINCE
    @Test
    @Throws(Exception::class)
    fun getRequestWithSinceDateShouldBe304() {
        val eTag = (postedAt + 1000L).toString()
        val result = dropServer.receiveMessageBytes(shouldContainNoNewMessagesSinceDateUri, "")
        assertEquals(304, result.first)
        assertThat(result.third, hasSize(0))
        assertNotEquals(eTag, result.second)
    }

    // GET 204 SINCE
    @Test
    fun getRequestWithSinceDateForEmptyDropShouldBe204() {
        val current = System.currentTimeMillis().toString();
        val result = dropServer.receiveMessageBytes(notExistingUri, current)
        // Then
        assertEquals(204, result.first)
        assertNotEquals(current, result.second)
        assertThat(result.third, hasSize(0))
    }

}
