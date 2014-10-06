package de.qabel.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DropHTTPTest {

	public long postedAt = 0;
	private URL workingUrl;
	private URL tooShortUrl;
	private URL notExistingUrl;

	@Before
	public void setUp() {
		try {
			workingUrl = new URL(
					"http://localhost:6000/abcdefghijklmnopqrstuvwxyzabcdefghijklmnopo");

			tooShortUrl = new URL("http://localhost:6000/IAmTooShort");

			notExistingUrl = new URL(
					"http://localhost:6000/abcdefghijklmnopqrstuvwxyzabcdefghijklmnopq");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// POST 200
	@Test
	@Ignore
	public void postMessageOk() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		String message = "Hello Welt";
		// When
		int responseCode = dHTTP.send(this.workingUrl, message.getBytes());
		this.postedAt = System.currentTimeMillis();
		// Then
		assertEquals(200, responseCode);
	}

	// POST 400
	@Test
	@Ignore
	public void postMessageNotGivenOrInvalid() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		String message = "";
		// When
		int responseCode = dHTTP.send(this.workingUrl, message.getBytes());
		// Then
		assertEquals(400, responseCode);
	}

	// POST 413
	@Test
	@Ignore
	public void postMessageTooBig() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		char[] chars = new char[2001];
		Arrays.fill(chars, 'a');
		// When
		int responseCode = dHTTP.send(this.workingUrl,
				new String(chars).getBytes());
		// Then
		assertEquals(413, responseCode);
	}

	// GET 200
	@Test
	@Ignore
	public void getRequestShouldGetCompleteDrop() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		String response = dHTTP.receiveMessages(this.workingUrl);
		// Then
		assertNotEquals(null, response);
		assertNotEquals("", response);
	}

	// GET 400
	@Test
	@Ignore
	public void getRequestWithInvalidOrMissingDropIdShouldBe400() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		String response = dHTTP.receiveMessages(this.tooShortUrl);
		// Then
		assertNotEquals(null, response);
		assertEquals("", response);
	}

	// GET 404
	@Test
	@Ignore
	public void getRequestForEmptyDropShouldBe404() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		String response = dHTTP.receiveMessages(this.notExistingUrl);
		// Then
		assertNotEquals(null, response);
		assertEquals("", response);
	}

	// GET 200 SINCE
	@Test
	@Ignore
	public void getRequestShouldEntriesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		String response = dHTTP.receiveMessages(this.workingUrl, 0);
		// Then
		assertNotEquals(null, response);
		assertNotEquals("", response);
	}

	// GET 304 SINCE
	@Test
	@Ignore
	public void getRequestWithSinceDateShouldBe304() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		String response = dHTTP.receiveMessages(this.workingUrl,
				System.currentTimeMillis());
		// Then
		assertNotEquals(null, response);
		assertEquals("", response);
	}

	// GET 404 SINCE
	@Test
	@Ignore
	public void getRequestWithSinceDateForEmptyDropShouldBe404() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		String response = dHTTP.receiveMessages(this.notExistingUrl,
				System.currentTimeMillis());
		// Then
		assertNotEquals(null, response);
		assertEquals("", response);
	}

	// HEAD 200
	@Test
	@Ignore
	public void shouldContainMessages() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.workingUrl);
		// Then
		assertEquals(200, responseCode);
	}

	// HEAD 400
	@Test
	@Ignore
	public void shouldBeInvalidOrMissingDropId() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.tooShortUrl);
		// Then
		assertEquals(400, responseCode);
	}

	// HEAD 404
	@Test
	@Ignore
	public void shouldBeEmpty() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.notExistingUrl);
		// Then
		assertEquals(404, responseCode);
	}

	// HEAD 200 SINCE
	@Test
	@Ignore
	public void shouldContainNewMessagesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.workingUrl, this.postedAt);
		// Then
		assertEquals(200, responseCode);
	}

	// HEAD 304 SINCE
	@Test
	@Ignore
	public void shouldContainNoNewMessagesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.workingUrl,
				System.currentTimeMillis());
		// Then
		assertEquals(304, responseCode);
	}

	// HEAD 404 + SINCE
	@Test
	@Ignore
	public void shouldBeEmptyWithSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.notExistingUrl,
				System.currentTimeMillis() + 10);
		// Then
		assertEquals(404, responseCode);
	}

}
