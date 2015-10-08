package de.qabel.core.accounting;

import de.qabel.core.config.AccountingServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

public class AccountingHTTPTest {

	public AccountingServer server;
	private AccountingHTTP accountingHTTP;

	@Before
	public void setServer() throws URISyntaxException, IOException {
		server = new AccountingServer(new URI("http://localhost:9696"),
				"testuser", "testuser");
		accountingHTTP = new AccountingHTTP(server);
		assertTrue("Login to accounting server failed", accountingHTTP.login());
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
}
