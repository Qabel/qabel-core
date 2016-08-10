package de.qabel.core.accounting

import de.qabel.core.config.AccountingServer
import de.qabel.core.exceptions.QblCreateAccountFailException
import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.entity.BasicHttpEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.Random

import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Assert.*

class BoxHttpClientTest {

    lateinit var server: AccountingServer
    private var boxClient: BoxClient? = null
    private var profile: AccountingProfile? = null
    private var serverBuilder: TestAccountingServerBuilder? = null

    @Test
    fun testGetQuota() {
        val responseContent = "{\"quota\": 2147483648, \"size\": 15460}"
        val httpClient = stubClient("GET", "http://localhost:9697/api/v0/quota/", 200, responseContent)
        boxClient = BoxHttpClient(server, profile!!, httpClient)

        val expectedQuota = QuotaState(2147483648L, 15460)

        val quotaState = boxClient!!.quotaState

        assertEquals(expectedQuota.quota, quotaState.quota)
        assertEquals(expectedQuota.size, quotaState.size)
    }

    @Test
    fun testQuotaDescription() {
        val quota = QuotaState(2147483648L, 1073741824L)
        val expected = "1 GB free / 2 GB"
        assertEquals(expected, quota.toString())
    }

    @Before
    fun setServer() {
        serverBuilder = TestAccountingServerBuilder()
        server = serverBuilder!!.build()
        profile = AccountingProfile()
        boxClient = BoxHttpClient(server, profile!!)
        boxClient!!.login()
        boxClient!!.createPrefix()
    }

    @Test(expected = RuntimeException::class)
    fun testIllegalResource() {
        boxClient!!.buildUri("foo/")
    }

    @Test
    fun testBuildUrl() {
        val url = boxClient!!.buildUri("foobar").build()
        assertThat(url.toString(), endsWith("foobar/"))
        assertThat(url.toString(), startsWith(server.uri!!.toString()))

    }

    @Test
    fun testLogin() {
        assertNotNull("Auth token not set after login", server.authToken)
    }

    @Test(expected = QblInvalidCredentials::class)
    fun testLoginFailed() {
        server.authToken = null
        server.password = "foobar"
        boxClient!!.login()
    }

    @Test
    fun testAutologin() {
        server.authToken = null
        boxClient!!.quotaState
        assertNotNull(server.authToken)
    }

    @Test
    fun testGetPrefix() {
        assertNotNull(boxClient!!.prefixes)
        assertNotEquals(boxClient!!.prefixes.size.toLong(), 0)
    }

    @get:Rule
    var expectedEx = ExpectedException.none()

    @Test
    fun resetPasswordThrowsIllegalArgumentExceptionOnInvalidMail() {
        expectedEx.expectMessage("Enter a valid email address.")
        expectedEx.expect(IllegalArgumentException::class.java)

        val responseContent = "{\"email\": [\"Enter a valid email address.\"]}"
        val client = stubClient("POST", "http://localhost:9696/api/v0/auth/password/reset/", 400, responseContent)
        val http = BoxHttpClient(server, profile!!, client)
        http.resetPassword("mymail")
    }

    @Test
    fun resetsPassword() {
        val responseContent = "{\"success\":\"Password reset e-mail has been sent.\"}"
        val client = CloseableHttpClientStub()
        val response = createResponseFromString(200, responseContent)
        client.addResponse("POST", "http://localhost:9696/api/v0/auth/password/reset/", response)
        val http = BoxHttpClient(server, profile!!, client)
        http.resetPassword("valid.email@example.org")

        assertEquals("{\"email\":\"" + "valid.email@example.org" + "\"}", client.body)
        assertTrue(response.closed)
    }

    @Test(expected = IOException::class)
    fun resetPasswordConvertsFormatExceptions() {
        val responseContent = "invalid json"
        val client = stubClient("POST", "http://localhost:9696/api/v0/auth/password/reset/", 200, responseContent)
        val http = BoxHttpClient(server, profile!!, client)
        http.resetPassword("mymail")
    }

    @Test
    fun createBoxAccount() {
        val rand = Random()

        val name = "testUser" + rand.nextInt(10000)
        server = serverBuilder!!.user(name).build()
        boxClient = BoxHttpClient(server, profile!!)
        boxClient!!.createBoxAccount(name + "@example.com")
        boxClient!!.login()
        assertNotNull("Auth token not set after login", server.authToken)
    }

    @Test
    fun createBoxAccountEMailNotCorrect() {

        server = serverBuilder!!.user("testUser").build()
        boxClient = BoxHttpClient(server, profile!!)
        var map: Map<*, *>? = null
        try {
            boxClient!!.createBoxAccount("testUser")
            fail("No Exception thrown")
        } catch (e: QblCreateAccountFailException) {
            map = e.map
        }

        assertNotNull(map)
        assertTrue(map!!.containsKey("email"))
    }

    @Test
    fun createBoxAccountPsToShort() {

        server = serverBuilder!!.user("testUser").pass("12345").build()
        boxClient = BoxHttpClient(server, profile!!)
        var map: Map<*, *>? = null
        try {
            boxClient!!.createBoxAccount("testUser")
            fail("No Exception thrown")
        } catch (e: QblCreateAccountFailException) {
            map = e.map
        }

        assertNotNull(map)
        assertTrue(map!!.containsKey("password1"))
    }

    @Test
    fun createBoxAccountUsernameAlreadyInUse() {

        server = serverBuilder!!.user("testuser").build()
        boxClient = BoxHttpClient(server, profile!!)
        var map: Map<*, *>? = null
        try {
            boxClient!!.createBoxAccount("testUser")
            fail("No Exception thrown")
        } catch (e: QblCreateAccountFailException) {
            map = e.map
        }

        assertNotNull(map)
        assertTrue(map!!.containsKey("username"))
    }

    @Test
    fun createBoxAccountEmailAlreadyInUse() {

        server = serverBuilder!!.user("testuser").build()
        boxClient = BoxHttpClient(server, profile!!)
        var map: Map<*, *>? = null
        try {
            boxClient!!.createBoxAccount("testuser")
            fail("No Exception thrown")
        } catch (e: QblCreateAccountFailException) {
            map = e.map
        }

        assertNotNull(map)
        assertTrue(map!!.containsKey("email"))
    }

    private fun stubClient(method: String, uri: String, statusCode: Int, responseContent: String): CloseableHttpClientStub {
        val client = CloseableHttpClientStub()
        val response = createResponseFromString(statusCode, responseContent)
        client.addResponse(method, uri, response)
        return client
    }

    private fun createResponseFromString(statusCode: Int, responseContent: String): CloseableHttpResponseStub {
        val response = CloseableHttpResponseStub()
        response.setStatusCode(statusCode)
        val entity = BasicHttpEntity()
        entity.content = ByteArrayInputStream(responseContent.toByteArray())
        response.setEntity(entity)
        return response
    }
}
