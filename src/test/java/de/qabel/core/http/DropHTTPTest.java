package de.qabel.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class DropHTTPTest {

	public long postedAt = 0;
	private URL workingUrl;
	private URL tooShortUrl;
	private URL notExistingUrl;
    private URL shouldContainMessagesUrl;
    private URL shouldContainNoNewMessagesSinceDateUrl;

	@Before
	public void setUp() {
		try {
			workingUrl = new URL(
					"http://localhost:6000/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl");

			tooShortUrl = new URL("http://localhost:6000/IAmTooShort");

			notExistingUrl = new URL(
					"http://localhost:6000/abcdefghijklmnopqrstuvwxyzabcnotExistingUrl");

            shouldContainMessagesUrl = new URL(
                    "http://localhost:6000/abcdefghijklmnopqrstuvshouldContainMessages");

            shouldContainNoNewMessagesSinceDateUrl = new URL(
                    "http://localhost:6000/abcdefghshouldContainNoNewMessagesSinceDate");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //prepare dropserver content for tests.
        DropHTTP h = new DropHTTP();
        h.send(shouldContainMessagesUrl, "shouldContainMessagesTestMessage".getBytes());
        h.send(shouldContainNoNewMessagesSinceDateUrl, "shouldContainNoNewMessagesSinceDate".getBytes());

	}

	// POST 200
	@Test
	public void postMessageOk() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		String message = "Test";
		// When
		HTTPResult<?> result = dHTTP.send(this.workingUrl, message.getBytes());
		this.postedAt = System.currentTimeMillis();
		// Then
		assertEquals(200, result.getResponseCode());
		assertTrue(result.isOk());
	}

	// POST 400
	@Test
	public void postMessageNotGivenOrInvalid() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		String message = "";
		// When
		HTTPResult<?> result = dHTTP.send(this.workingUrl, message.getBytes());
		// Then
		assertEquals(400, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// POST 413
	@Test
	public void postMessageTooBig() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		char[] chars = new char[2574]; // one byte more than the server accepts
		Arrays.fill(chars, 'a');
		// When
		HTTPResult<?> result = dHTTP.send(this.workingUrl,
				new String(chars).getBytes());
		// Then
		assertEquals(413, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// GET 200
	@Test
	public void getRequestShouldGetCompleteDrop() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.workingUrl);
		// Then
		assertNotEquals(null, result.getData());
		assertNotEquals(new ArrayList<byte[]>(), result.getData());
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	// GET 400
	@Test
	public void getRequestWithInvalidOrMissingDropIdShouldBe400() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.tooShortUrl);
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<String>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	// GET 204
	@Test
	public void getRequestForEmptyDropShouldBe204() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.notExistingUrl);
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<String>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	// GET 200 SINCE
	@Test
	public void getRequestShouldEntriesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.shouldContainMessagesUrl, 0);
		// Then
		assertNotEquals(null, result.getData());
		assertNotEquals(new ArrayList<byte[]>(), result.getData());
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	// GET 304 SINCE
	@Test
	public void getRequestWithSinceDateShouldBe304() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.workingUrl,
				System.currentTimeMillis());
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<String>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(304, result.getResponseCode());
	}

	// GET 204 SINCE
	@Test
	public void getRequestWithSinceDateForEmptyDropShouldBe204() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.notExistingUrl,
				System.currentTimeMillis());
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<String>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	// HEAD 200
	@Test
	public void shouldContainMessages() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.shouldContainMessagesUrl);
		// Then
		assertEquals(200, result.getResponseCode());
		assertTrue(result.isOk());
	}

	// HEAD 400
	@Test
	public void shouldBeInvalidOrMissingDropId() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.tooShortUrl);
		// Then
		assertEquals(400, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// HEAD 204
	@Test
	public void shouldBeEmpty() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.notExistingUrl);
		// Then
		assertEquals(204, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// HEAD 200 SINCE
	@Test
	public void shouldContainNewMessagesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.workingUrl, this.postedAt);
		// Then
		assertEquals(200, result.getResponseCode());
		assertTrue(result.isOk());
	}

	// HEAD 304 SINCE
	@Test
	public void shouldContainNoNewMessagesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.shouldContainNoNewMessagesSinceDateUrl,
				System.currentTimeMillis());
		// Then
		assertEquals(304, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// HEAD 404 + SINCE
	@Test
	public void shouldBeEmptyWithSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.notExistingUrl,
				System.currentTimeMillis() + 10);
		// Then
		assertEquals(204, result.getResponseCode());
		assertFalse(result.isOk());
	}

}
