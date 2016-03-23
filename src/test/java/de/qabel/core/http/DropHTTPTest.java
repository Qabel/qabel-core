package de.qabel.core.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DropHTTPTest {

	public long postedAt = 0;
	private URI workingUri, tooShortUri, notExistingUri,
			shouldContainMessagesUri, shouldContainNoNewMessagesSinceDateUri;

	@Before
	public void setUp() {
		try {
			workingUri = new URI(
					"http://localhost:5001/abcdefghijklmnopqrstuvwxyzabcdefgworkingUrl");

			tooShortUri = new URI("http://localhost:5001/IAmTooShort");

			notExistingUri = new URI(
					"http://localhost:5001/abcdefghijklmnopqrstuvwxyzabcnotExistingUrl");

            shouldContainMessagesUri = new URI(
                    "http://localhost:5001/abcdefghijklmnopqrstuvshouldContainMessages");

            shouldContainNoNewMessagesSinceDateUri = new URI(
                    "http://localhost:5001/xbcdefghshouldContainNoNewMessagesSinceDate");

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		//prepare dropserver content for tests.
        DropHTTP h = new DropHTTP();
        h.send(shouldContainMessagesUri, "shouldContainMessagesTestMessage".getBytes());
        h.send(shouldContainNoNewMessagesSinceDateUri, "shouldContainNoNewMessagesSinceDate".getBytes());
	}

	// POST 200
	@Test
	public void postMessageOk() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		String message = "Test";
		// When
		HTTPResult<?> result = dHTTP.send(this.workingUri, message.getBytes());
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
		HTTPResult<?> result = dHTTP.send(this.workingUri, message.getBytes());
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
		HTTPResult<?> result = dHTTP.send(this.workingUri,
				new String(chars).getBytes());
		// Then
		assertEquals(413, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// GET 200
	@Test
	public void getRequestShouldGetCompleteDrop() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.workingUri);
		// Then
		assertNotEquals(null, result.getData());
		assertNotEquals(new ArrayList<byte[]>(), result.getData());
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
		assertNotNull("missing Last-Modified information", result.lastModified());
	}

	// GET 400
	@Test
	public void getRequestWithInvalidOrMissingDropIdShouldBe400() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.tooShortUri);
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<byte[]>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	// GET 204
	@Test
	public void getRequestForEmptyDropShouldBe204() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.notExistingUri);
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<byte[]>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	// GET 200 SINCE
	@Test
	public void getRequestShouldEntriesSinceDate() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.shouldContainMessagesUri, 0);
		// Then
		assertNotEquals(null, result.getData());
		assertNotEquals(new ArrayList<byte[]>(), result.getData());
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	// GET 304 SINCE
	@Test
	public void getRequestWithSinceDateShouldBe304() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.shouldContainNoNewMessagesSinceDateUri,
				System.currentTimeMillis() + 1000L);
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<byte[]>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(304, result.getResponseCode());
	}

	// GET 204 SINCE
	@Test
	public void getRequestWithSinceDateForEmptyDropShouldBe204() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<Collection<byte[]>> result = dHTTP.receiveMessages(this.notExistingUri,
				System.currentTimeMillis());
		// Then
		assertNotEquals(null, result.getData());
		assertEquals(new ArrayList<byte[]>(), result.getData());
		assertFalse(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	// HEAD 200
	@Test
	public void shouldContainMessages() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.shouldContainMessagesUri);
		// Then
		assertEquals(200, result.getResponseCode());
		assertTrue(result.isOk());
	}

	// HEAD 400
	@Test
	public void shouldBeInvalidOrMissingDropId() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.tooShortUri);
		// Then
		assertEquals(400, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// HEAD 204
	@Test
	public void shouldBeEmpty() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.notExistingUri);
		// Then
		assertEquals(204, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// HEAD 200 SINCE
	@Test
	public void shouldContainNewMessagesSinceDate() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.workingUri, this.postedAt);
		// Then
		assertEquals(200, result.getResponseCode());
		assertTrue(result.isOk());
	}

	// HEAD 304 SINCE
	@Test
	public void shouldContainNoNewMessagesSinceDate() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.shouldContainNoNewMessagesSinceDateUri,
				System.currentTimeMillis() + 1000L);
		// Then
		assertEquals(304, result.getResponseCode());
		assertFalse(result.isOk());
	}

	// HEAD 204 + SINCE
	@Test
	public void shouldBeEmptyWithSinceDate() throws Exception {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		HTTPResult<?> result = dHTTP.head(this.notExistingUri,
				System.currentTimeMillis() + 10);
		// Then
		assertEquals(204, result.getResponseCode());
		assertFalse(result.isOk());
	}

}
