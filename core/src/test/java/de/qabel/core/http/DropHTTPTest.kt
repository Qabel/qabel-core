package de.qabel.core.http

import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Arrays

import org.junit.Assert.*

@Ignore
class DropHTTPTest {

    var postedAt: Long = 0
    private var workingUri: URI? = null
    private var tooShortUri: URI? = null
    private var notExistingUri: URI? = null
    private var shouldContainMessagesUri: URI? = null
    private var shouldContainNoNewMessagesSinceDateUri: URI? = null

    @Before
    fun setUp() {
        try {
            workingUri = URI(
                    "http://localhost:5000/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl")

            tooShortUri = URI("http://localhost:5000/IAmTooShort")

            notExistingUri = URI(
                    "http://localhost:5000/abcdefghijklmnopqrstuvwxyzabcnotExistingUrl")

            shouldContainMessagesUri = URI(
                    "http://localhost:5000/abcdefghijklmnopqrstuvshouldContainMessages")

            shouldContainNoNewMessagesSinceDateUri = URI(
                    "http://localhost:5000/xbcdefghshouldContainNoNewMessagesSinceDate")

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        //prepare dropserver content for tests.
        val h = DropHTTP()
        h.send(shouldContainMessagesUri!!, "shouldContainMessagesTestMessage".toByteArray())
        h.send(shouldContainNoNewMessagesSinceDateUri!!, "shouldContainNoNewMessagesSinceDate".toByteArray())
    }

    // POST 200
    @Test
    @Throws(Exception::class)
    fun postMessageOk() {
        // Given
        val dHTTP = DropHTTP()
        val message = "Test"
        // When
        val result = dHTTP.send(workingUri!!, message.toByteArray())
        postedAt = System.currentTimeMillis()
        // Then
        assertEquals(200, result.responseCode.toLong())
        assertTrue(result.isOk)

        assertTrue(dHTTP.receiveMessages(workingUri!!).data!!.contains(message.toByteArray()))
    }

    // POST 400
    @Test
    fun postMessageNotGivenOrInvalid() {
        // Given
        val dHTTP = DropHTTP()
        val message = ""
        // When
        val result = dHTTP.send(workingUri!!, message.toByteArray())
        // Then
        assertEquals(400, result.responseCode.toLong())
        assertFalse(result.isOk)
    }

    // POST 413
    @Test
    fun postMessageTooBig() {
        // Given
        val dHTTP = DropHTTP()
        val chars = CharArray(2574) // one byte more than the server accepts
        Arrays.fill(chars, 'a')
        // When
        val result = dHTTP.send(workingUri!!,
                String(chars).toByteArray())
        // Then
        assertEquals(413, result.responseCode.toLong())
        assertFalse(result.isOk)
    }

    // GET 200
    @Test
    @Throws(Exception::class)
    fun getRequestShouldGetCompleteDrop() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.receiveMessages(workingUri!!)
        // Then
        assertNotEquals(null, result.data)
        assertNotEquals(ArrayList<ByteArray>(), result.data)
        assertTrue(result.isOk)
        assertEquals(200, result.responseCode.toLong())
        assertNotNull("missing Last-Modified information", result.lastModified())
    }

    // GET 400
    @Test
    @Throws(Exception::class)
    fun getRequestWithInvalidOrMissingDropIdShouldBe400() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.receiveMessages(tooShortUri!!)
        // Then
        assertNotEquals(null, result.data)
        assertEquals(ArrayList<ByteArray>(), result.data)
        assertFalse(result.isOk)
        assertEquals(400, result.responseCode.toLong())
    }

    // GET 204
    @Test
    @Throws(Exception::class)
    fun getRequestForEmptyDropShouldBe204() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.receiveMessages(notExistingUri!!)
        // Then
        assertNotEquals(null, result.data)
        assertEquals(ArrayList<ByteArray>(), result.data)
        assertFalse(result.isOk)
        assertEquals(204, result.responseCode.toLong())
    }

    // GET 200 SINCE
    @Test
    @Throws(Exception::class)
    fun getRequestShouldEntriesSinceDate() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.receiveMessages(shouldContainMessagesUri!!, 0)
        // Then
        assertNotEquals(null, result.data)
        assertNotEquals(ArrayList<ByteArray>(), result.data)
        assertTrue(result.isOk)
        assertEquals(200, result.responseCode.toLong())
    }

    // GET 304 SINCE
    @Test
    @Throws(Exception::class)
    fun getRequestWithSinceDateShouldBe304() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.receiveMessages(shouldContainNoNewMessagesSinceDateUri!!,
                System.currentTimeMillis() + 1000L)
        // Then
        assertNotEquals(null, result.data)
        assertEquals(ArrayList<ByteArray>(), result.data)
        assertFalse(result.isOk)
        assertEquals(304, result.responseCode.toLong())
    }

    // GET 204 SINCE
    @Test
    @Throws(Exception::class)
    fun getRequestWithSinceDateForEmptyDropShouldBe204() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.receiveMessages(notExistingUri!!,
                System.currentTimeMillis())
        // Then
        assertNotEquals(null, result.data)
        assertEquals(ArrayList<ByteArray>(), result.data)
        assertFalse(result.isOk)
        assertEquals(204, result.responseCode.toLong())
    }

    // HEAD 200
    @Test
    @Throws(Exception::class)
    fun shouldContainMessages() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.head(shouldContainMessagesUri!!)
        // Then
        assertEquals(200, result.responseCode.toLong())
        assertTrue(result.isOk)
    }

    // HEAD 400
    @Test
    @Throws(Exception::class)
    fun shouldBeInvalidOrMissingDropId() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.head(tooShortUri!!)
        // Then
        assertEquals(400, result.responseCode.toLong())
        assertFalse(result.isOk)
    }

    // HEAD 204
    @Test
    @Throws(Exception::class)
    fun shouldBeEmpty() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.head(notExistingUri!!)
        // Then
        assertEquals(204, result.responseCode.toLong())
        assertFalse(result.isOk)
    }

    // HEAD 200 SINCE
    @Test
    @Throws(Exception::class)
    fun shouldContainNewMessagesSinceDate() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.head(workingUri!!, postedAt)
        // Then
        assertEquals(200, result.responseCode.toLong())
        assertTrue(result.isOk)
    }

    // HEAD 304 SINCE
    @Test
    @Throws(Exception::class)
    fun shouldContainNoNewMessagesSinceDate() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.head(shouldContainNoNewMessagesSinceDateUri!!,
                System.currentTimeMillis() + 1000L)
        // Then
        assertEquals(304, result.responseCode.toLong())
        assertFalse(result.isOk)
    }

    // HEAD 204 + SINCE
    @Test
    @Throws(Exception::class)
    fun shouldBeEmptyWithSinceDate() {
        // Given
        val dHTTP = DropHTTP()
        // When
        val result = dHTTP.head(notExistingUri!!,
                System.currentTimeMillis() + 10)
        // Then
        assertEquals(204, result.responseCode.toLong())
        assertFalse(result.isOk)
    }

}
