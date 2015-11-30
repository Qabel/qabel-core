package de.qabel.core.accounting;

import com.amazonaws.auth.BasicSessionCredentials;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblInvalidCredentials;
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
	public void setServer() throws URISyntaxException, IOException, QblInvalidCredentials {
		server = new AccountingServer(new URI("http://localhost:9696"),
				"testuser", "testuser");
		accountingHTTP = new AccountingHTTP(server, new AccountingProfile());
		accountingHTTP.login();
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
	public void testGetCredentials() throws IOException, QblInvalidCredentials {
		BasicSessionCredentials credentials = accountingHTTP.getCredentials();
		assertNotNull(credentials);
		assertNotNull("No AWS key", credentials.getAWSAccessKeyId());
		assertNotNull("No AWS secret key", credentials.getAWSSecretKey());
		assertNotNull("No AWS token", credentials.getSessionToken());
	}

	@Test
	public void testGetPrefix() throws IOException, QblInvalidCredentials {
		assertNotNull(accountingHTTP.getPrefixes());
	}

	@Test
	public void testUpdateProfile() throws IOException, QblInvalidCredentials {
		accountingHTTP.updateProfile();
		assertNotNull(accountingHTTP.getProfile());
	}


}
