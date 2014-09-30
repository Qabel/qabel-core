package de.qabel.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.ModelObject;

public class DropHTTPTest {
	private URL url;
	private URL tooShortUrl;
	private URL notExistingUrl;
	public long postedAt = 0;

	@Before
	public void setUp() {
		try {
			url = new URL(
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
	public void postMessageOk() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		DropMessage<ModelObject> message = new DropMessage<ModelObject>(0,
				new Date(), null, "Me", null, null);
		// When
		int responseCode = dHTTP.send(this.url, message);
		this.postedAt = System.currentTimeMillis();
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(200, responseCode);
		assertTrue(body.isEmpty());
	}

	// POST 400
	@Test
	public void postMessageNotGivenOrInvalid() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		DropMessage<ModelObject> message = new DropMessage<ModelObject>(0,
				new Date(), null, "Me", null, null);
		// When
		int responseCode = dHTTP.send(this.url, message);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(400, responseCode);
		assertTrue(body.isEmpty());
	}

	// POST 413
	@Test
	public void postMessageTooBig() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		DropMessage<ModelObject> message = new DropMessage<ModelObject>(0,
				new Date(), null, "Me", null, null);
		// When
		int responseCode = dHTTP.send(this.url, message);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(413, responseCode);
		assertTrue(body.isEmpty());
	}

	// GET 200
	@Test
	public void getRequestShouldGetCompleteDrop() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.receiveMessages(this.url);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(200, responseCode);
		assertFalse(body.isEmpty());
	}

	// GET 400
	@Test
	public void getRequestWithInvalidOrMissingDropIdShouldBe400() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.receiveMessages(this.tooShortUrl);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(400, responseCode);
		assertTrue(body.isEmpty());
	}

	// GET 404
	@Test
	public void getRequestForEmptyDropShouldBe404() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.receiveMessages(this.notExistingUrl);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(404, responseCode);
		assertTrue(body.isEmpty());
	}

	// GET 200 SINCE
	// TODO Check HTTPBody, if it contains the right messages.
	@Test
	public void getRequestShouldEntriesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.receiveMessages(this.url, 0);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(200, responseCode);
		assertTrue(body.isEmpty());
	}

	// GET 304 SINCE
	@Test
	public void getRequestWithSinceDateShouldBe304() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.receiveMessages(this.url,
				System.currentTimeMillis());
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(304, responseCode);
		assertTrue(body.isEmpty());
	}

	// GET 404 SINCE
	@Test
	public void getRequestWithSinceDateForEmptyDropShouldBe404() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.receiveMessages(this.notExistingUrl,
				System.currentTimeMillis());
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(404, responseCode);
		assertTrue(body.isEmpty());
	}

	// HEAD 200
	@Test
	public void shouldContainMessages() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.url);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(200, responseCode);
		assertTrue(body.isEmpty());
	}

	// HEAD 400
	@Test
	public void shouldBeInvalidOrMissingDropId() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.tooShortUrl);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(400, responseCode);
		assertTrue(body.isEmpty());
	}

	// HEAD 404
	@Test
	public void shouldBeEmpty() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.notExistingUrl);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(404, responseCode);
		assertTrue(body.isEmpty());
	}

	// HEAD 200 SINCE
	@Test
	public void shouldContainNewMessagesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.url, this.postedAt);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(200, responseCode);
		assertTrue(body.isEmpty());
	}

	// HEAD 304 SINCE
	@Test
	public void shouldContainNoNewMessagesSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		// When
		int responseCode = dHTTP.head(this.url, System.currentTimeMillis());
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(304, responseCode);
		assertTrue(body.isEmpty());
	}

	// HEAD 404 + SINCE
	@Test
	public void shouldBeEmptyWithSinceDate() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.head(this.notExistingUrl,
				System.currentTimeMillis() + 10);
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(404, responseCode);
		assertTrue(body.isEmpty());
	}

}
