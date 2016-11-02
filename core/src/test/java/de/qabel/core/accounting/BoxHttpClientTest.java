package de.qabel.core.accounting;

import de.qabel.core.TestServer;
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

public class BoxHttpClientTest {

    public AccountingServer server;
    private BoxClient boxClient;
    private AccountingProfile profile;
    private TestAccountingServerBuilder serverBuilder;

    @Before
    public void setServer() throws Exception {
        serverBuilder = new TestAccountingServerBuilder();
        server = serverBuilder.build();
        profile = new AccountingProfile();
        boxClient = new BoxHttpClient(server, profile);
        boxClient.login();
        boxClient.createPrefix();
    }

    @Test
    public void testGetQuota() throws Exception {
        String responseContent = "{\"quota\": 2147483648, \"size\": 15460}";
        CloseableHttpClientStub httpClient = stubClient("GET", TestServer.BLOCK + "/api/v0/quota/", 200, responseContent);
        boxClient = new BoxHttpClient(server, profile, httpClient);

        QuotaState expectedQuota = new QuotaState(2147483648L, 15460);

        QuotaState quotaState = boxClient.getQuotaState();

        assertEquals(expectedQuota.getQuota(), quotaState.getQuota());
        assertEquals(expectedQuota.getSize(), quotaState.getSize());
    }

    @Test
    public void testQuotaDescription() {
        QuotaState quota = new QuotaState(2147483648L, 1073741824L);
        String expected = "1 GB free / 2 GB";
        assertEquals(expected, quota.toString());
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalResource() {
        boxClient.buildUri("foo/");
    }

    @Test
    public void testBuildUrl() throws URISyntaxException {
        URI url = boxClient.buildUri("foobar").build();
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
        boxClient.login();
    }

    @Test
    public void testAutologin() throws IOException, QblInvalidCredentials {
        server.setAuthToken(null);
        boxClient.getQuotaState();
        assertNotNull(server.getAuthToken());
    }

    @Test
    public void testGetPrefix() throws IOException, QblInvalidCredentials {
        assertNotNull(boxClient.getPrefixes());
        assertNotEquals(boxClient.getPrefixes().size(), 0);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void resetPasswordThrowsIllegalArgumentExceptionOnInvalidMail() throws Exception {
        expectedEx.expectMessage("Enter a valid email address.");
        expectedEx.expect(IllegalArgumentException.class);

        String responseContent = "{\"email\": [\"Enter a valid email address.\"]}";
        CloseableHttpClientStub client = stubClient("POST", TestServer.ACCOUNTING + "/api/v0/auth/password/reset/", 400, responseContent);
        BoxClient http = new BoxHttpClient(server, profile, client);
        http.resetPassword("mymail");
    }

    @Test
    public void resetsPassword() throws Exception {
        String responseContent = "{\"success\":\"Password reset e-mail has been sent.\"}";
        CloseableHttpClientStub client = new CloseableHttpClientStub();
        CloseableHttpResponseStub response = createResponseFromString(200, responseContent);
        client.addResponse("POST", TestServer.ACCOUNTING + "/api/v0/auth/password/reset/", response);
        BoxClient http = new BoxHttpClient(server, profile, client);
        http.resetPassword("valid.email@example.org");

        assertEquals("{\"email\":\"" + "valid.email@example.org" + "\"}", client.getBody());
        assertTrue(response.closed);
    }

    @Test(expected = IOException.class)
    public void resetPasswordConvertsFormatExceptions() throws Exception {
        String responseContent = "invalid json";
        CloseableHttpClientStub client = stubClient("POST", TestServer.ACCOUNTING + "/api/v0/auth/password/reset/", 200, responseContent);
        BoxClient http = new BoxHttpClient(server, profile, client);
        http.resetPassword("mymail");
    }

    @Test
    public void createBoxAccount() throws Exception {
        Random rand = new Random();

        String name = "testUser" + rand.nextInt(10000);
        server = serverBuilder.user(name).build();
        boxClient = new BoxHttpClient(server, profile);
        boxClient.createBoxAccount(name + "@example.com");
        boxClient.login();
        assertNotNull("Auth token not set after login", server.getAuthToken());
    }

    @Test
    public void createBoxAccountEMailNotCorrect() throws Exception {

        server = serverBuilder.user("testUser").build();
        boxClient = new BoxHttpClient(server, profile);
        Map map = null;
        try {
            boxClient.createBoxAccount("testUser");
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
        boxClient = new BoxHttpClient(server, profile);
        Map map = null;
        try {
            boxClient.createBoxAccount("testUser");
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
        boxClient = new BoxHttpClient(server, profile);
        Map map = null;
        try {
            boxClient.createBoxAccount("testUser");
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
        boxClient = new BoxHttpClient(server, profile);
        Map map = null;
        try {
            boxClient.createBoxAccount("testuser");
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
