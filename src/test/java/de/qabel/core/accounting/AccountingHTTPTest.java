package de.qabel.core.accounting;

import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

public class AccountingHTTPTest {

	public AccountingServer server;
	private AccountingHTTP accountingHTTP;
	private AccountingProfile profile;
	private TestAccountingServerBuilder serverBuilder;

	@Before
	public void setServer() throws URISyntaxException, IOException, QblInvalidCredentials {
		serverBuilder = new TestAccountingServerBuilder();
		server = serverBuilder.build();
		profile = new AccountingProfile();
		accountingHTTP = new AccountingHTTP(server, profile);
		accountingHTTP.login();
		accountingHTTP.createPrefix();
	}

	@Test(expected = RuntimeException.class)
	public void testIllegalResource() {
		accountingHTTP.buildUri("foo/");
	}

	@Test
	public void testBuildUrl() throws URISyntaxException {
		URI url = accountingHTTP.buildUri("foobar").build();
		assertThat(url.toString(), endsWith("foobar/"));
		assertThat(url.toString(), startsWith(server.getUri().toString()));

	}

	@Test
	public void testLogin() {
		assertNotNull("Auth token not set after login", server.getAuthToken());
	}

	@Test(expected = QblInvalidCredentials.class)
	public void testLoginFailed() throws IOException, QblInvalidCredentials {
		server.setAuthToken(null);
		server.setPassword("foobar");
		accountingHTTP.login();
	}

	@Test
	public void testGetQuota() throws IOException, QblInvalidCredentials {
		assertEquals(100, accountingHTTP.getQuota());
	}

	@Test
	public void testAutologin() throws IOException, QblInvalidCredentials {
		server.setAuthToken(null);
		accountingHTTP.getQuota();
		assertNotNull(server.getAuthToken());
	}

	@Test
	public void testGetPrefix() throws IOException, QblInvalidCredentials {
		assertNotNull(accountingHTTP.getPrefixes());
		assertNotEquals(accountingHTTP.getPrefixes().size(), 0);
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void resetPasswordThrowsIllegalArgumentExceptionOnInvalidMail() throws Exception {
		expectedEx.expectMessage("Enter a valid email address.");
		expectedEx.expect(IllegalArgumentException.class);

		String responseContent = "{\"email\": [\"Enter a valid email address.\"]}";
		CloseableHttpClientStub client = stubClient("POST", "http://localhost:9696/api/v0/auth/password/reset/", 400, responseContent);
		AccountingHTTP http = new AccountingHTTP(server, profile, client);
		http.resetPassword("mymail");
	}

	@Test
	public void resetsPassword() throws Exception {
		String responseContent = "{\"success\":\"Password reset e-mail has been sent.\"}";
		CloseableHttpClientStub client = new CloseableHttpClientStub();
		CloseableHttpResponseStub response = createResponseFromString(200, responseContent);
		client.addResponse("POST", "http://localhost:9696/api/v0/auth/password/reset/", response);
		AccountingHTTP http = new AccountingHTTP(server, profile, client);
		http.resetPassword("valid.email@example.org");

		assertEquals("{\"email\":\"" + "valid.email@example.org" + "\"}", client.getBody());
		assertTrue(response.closed);
	}

	@Test(expected = IOException.class)
	public void resetPasswordConvertsFormatExceptions() throws Exception {
		String responseContent = "invalid json";
		CloseableHttpClientStub client = stubClient("POST", "http://localhost:9696/api/v0/auth/password/reset/", 200, responseContent);
		AccountingHTTP http = new AccountingHTTP(server, profile, client);
		http.resetPassword("mymail");
	}

	@Test
	public void createBoxAccount() throws Exception {
		Random rand = new Random();

		String name = "testUser" + rand.nextInt(10000);
		server = serverBuilder.user(name).build();
		accountingHTTP = new AccountingHTTP(server, profile);
		accountingHTTP.createBoxAccount(name + "@example.com");
		accountingHTTP.login();
		assertNotNull("Auth token not set after login", server.getAuthToken());
	}

	@Test
	public void createBoxAccountEMailNotCorrect() throws Exception {

		server = serverBuilder.user("testUser").build();
		accountingHTTP = new AccountingHTTP(server, profile);
		Map map = null;
		try {
			accountingHTTP.createBoxAccount("testUser");
			fail("No Exception thrown");
		} catch (QblCreateAccountFailException e) {
			map = e.getMap();
		}
		assertNotNull(map);
		assertTrue(map.containsKey("email"));
	}

	@Test
	public void createBoxAccountPsToShort() throws Exception {

		server = serverBuilder.user("testUser").pass("12345").build();
		accountingHTTP = new AccountingHTTP(server, profile);
		Map map = null;
		try {
			accountingHTTP.createBoxAccount("testUser");
			fail("No Exception thrown");
		} catch (QblCreateAccountFailException e) {
			map = e.getMap();
		}
		assertNotNull(map);
		assertTrue(map.containsKey("password1"));
	}

	@Test
	public void createBoxAccountUsernameAlreadyInUse() throws Exception {

		server = serverBuilder.user("testuser").build();
		accountingHTTP = new AccountingHTTP(server, profile);
		Map map = null;
		try {
			accountingHTTP.createBoxAccount("testUser");
			fail("No Exception thrown");
		} catch (QblCreateAccountFailException e) {
			map = e.getMap();
		}
		assertNotNull(map);
		assertTrue(map.containsKey("username"));
	}

	@Test
	public void createBoxAccountEmailAlreadyInUse() throws Exception {

		server = serverBuilder.user("testuser").build();
		accountingHTTP = new AccountingHTTP(server, profile);
		Map map = null;
		try {
			accountingHTTP.createBoxAccount("testuser");
			fail("No Exception thrown");
		} catch (QblCreateAccountFailException e) {
			map = e.getMap();
		}
		assertNotNull(map);
		assertTrue(map.containsKey("email"));
	}

	private CloseableHttpClientStub stubClient(String method, String uri, int statusCode, String responseContent) {
		CloseableHttpClientStub client = new CloseableHttpClientStub();
		CloseableHttpResponse response = createResponseFromString(statusCode, responseContent);
		client.addResponse(method, uri, response);
		return client;
	}

	private CloseableHttpResponseStub createResponseFromString(int statusCode, String responseContent) {
		CloseableHttpResponseStub response = new CloseableHttpResponseStub();
		response.setStatusCode(statusCode);
		BasicHttpEntity entity = new BasicHttpEntity();
		entity.setContent(new ByteArrayInputStream(responseContent.getBytes()));
		response.setEntity(entity);
		return response;
	}
}
