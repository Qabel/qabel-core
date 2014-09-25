package de.qabel.core.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.ModelObject;

public class DropHTTPTest {

	// POST 200
	@Test
	public void postMessageOk() {
		// Given
		DropHTTP dHTTP = new DropHTTP();
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DropMessage<ModelObject> message = new DropMessage<ModelObject>(0,
				new Date(), null, "Me", null, null);
		// When
		int responseCode = dHTTP.send(message);
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DropMessage<ModelObject> message = new DropMessage<ModelObject>(0,
				new Date(), null, "Me", null, null);
		// When
		int responseCode = dHTTP.send(message);
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DropMessage<ModelObject> message = new DropMessage<ModelObject>(0,
				new Date(), null, "Me", null, null);
		// When
		int responseCode = dHTTP.send(message);
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When
		int responseCode = dHTTP.receive();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When
		int responseCode = dHTTP.receive();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When
		int responseCode = dHTTP.receive();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.receive();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.receive();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.receive();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When
		int responseCode = dHTTP.head();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When
		int responseCode = dHTTP.head();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// When
		int responseCode = dHTTP.head();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.head();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.head();
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
		try {
			dHTTP.setURL(new URL("http://localhost:1337/drop"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dHTTP.setSinceDate(new Date());
		// When
		int responseCode = dHTTP.head();
		ArrayList<String> body = dHTTP.getHTTPBody();
		// Then
		assertEquals(404, responseCode);
		assertTrue(body.isEmpty());
	}

}
