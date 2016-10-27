package de.qabel.core.index.server

import de.qabel.core.TestServer
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.extensions.assertThrows
import de.qabel.core.index.*
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpUriRequest
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import java.util.*

@Ignore
class IndexHTTPTest {
    private val server = IndexHTTPLocation(TestServer.INDEX)
    private val index = IndexHTTP(server)

    companion object {
        fun getRandomMail(): String {
            return "test-%s@example.net".format(UUID.randomUUID())
        }
    }

    @Test
    fun testSearchNoResults() {
        val randomMail = getRandomMail()
        val result = index.search(mapOf(Pair(FieldType.EMAIL, randomMail)))
        assertEquals(0, result.size)
    }

    @Test
    fun testRetrieveServerPublicKey() {
        val publicKey = index.retrieveServerPublicKey()
        assertEquals(QblECPublicKey.KEY_SIZE_BYTE, publicKey.key.size)
    }

    @Test
    fun testUpdate() {
        val testParts = UpdateTestParts(index)
        testParts.publishTest()
        testParts.unpublishTest()
    }

    @Test
    fun testUpdateRetry() {
        // Positive test: with one "wronged" call everything still works.
        val outdatedKey = OutdatedKey(1, ServerPublicKeyEndpointImpl(server))
        val index = IndexHTTP(server, key = outdatedKey)
        val testParts = UpdateTestParts(index)
        testParts.publishTest()
        assertEquals(2, outdatedKey.numCalls)
        outdatedKey.numCalls = 0
        testParts.unpublishTest()
        assertEquals(2, outdatedKey.numCalls)
    }

    @Test
    fun testUpdateRetryFailure() {
        // Negative test: fails after too many retries
        val outdatedKey = OutdatedKey(3, ServerPublicKeyEndpointImpl(server))
        val index = IndexHTTP(server, key = outdatedKey)
        val testParts = UpdateTestParts(index)
        val exception = assertThrows(APIError::class) { testParts.publishTest() }
        assertEquals(3, outdatedKey.numCalls)
        assertEquals(2, exception.retries)
        assertEquals(400, exception.code)
    }

    private class UpdateTestParts(private val index: IndexHTTP) {
        private val mail1 = getRandomMail()
        private val mail2 = getRandomMail()

        val identity = UpdateIdentity(
            keyPair = QblECKeyPair(),
            dropURL = DropURL("http://example.net/somewhere/abcdefghijklmnopqrstuvwxyzabcdefghijklmnopo"),
            alias = "Major Anya, Unicode: 裸共產主義",
            fields = listOf(
                UpdateField(UpdateAction.CREATE, FieldType.EMAIL, mail1),
                UpdateField(UpdateAction.CREATE, FieldType.EMAIL, mail2)
            )
        )

        fun publishTest() {
            val updateResult = index.updateIdentity(identity)
            /* Note that this only holds if the shallow verification facet is enabled, otherwise this would be
             * ACCEPTED_DEFERRED to check the email.
            */
            assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, updateResult)

            /* Will be found with both mails */
            searchForMailAndAssertOurs(mail1)
            searchForMailAndAssertOurs(mail2)
        }

        fun unpublishTest() {
            val identity = identity.copy(
                fields = listOf(
                    UpdateField(UpdateAction.DELETE, FieldType.EMAIL, mail2)
                )
            )

            assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, index.updateIdentity(identity))

            searchForMailAndAssertOurs(mail1)  // still found
            assertEquals(0, index.search(mapOf(Pair(FieldType.EMAIL, mail2))).size)  // gone
        }

        fun assertIdentityEquals(identity: UpdateIdentity, indexContact: IndexContact) {
            assertEquals(identity.keyPair.pub, indexContact.publicKey)
            assertEquals(identity.alias, indexContact.alias)
            assertEquals(identity.dropURL, indexContact.dropUrl)
        }

        fun searchForMailAndAssertOurs(mail: String) {
            val search1Result = index.search(mapOf(Pair(FieldType.EMAIL, mail)))
            assertEquals(1, search1Result.size)
            val foundPublicIdentity = search1Result[0]
            assertIdentityEquals(identity, foundPublicIdentity)
        }
    }

    private class OutdatedKey(
        private val numWrongedCalls: Int = 1,
        private val implementation: ServerPublicKeyEndpoint,
        private val wrongServerPublicKey: QblECPublicKey = QblECKeyPair().pub
    ) : ServerPublicKeyEndpoint {
        var numCalls = 0

        override fun buildRequest(): HttpUriRequest {
            return implementation.buildRequest()
        }

        override fun parseResponse(jsonString: String, statusLine: StatusLine): QblECPublicKey {
            numCalls++
            if (numCalls <= numWrongedCalls) {
                return wrongServerPublicKey
            } else {
                return implementation.parseResponse(jsonString, statusLine)
            }
        }
    }

    @Test(expected = CodeInvalidException::class)
    fun testConfirmCodeInvalid() {
        index.confirmVerificationCode("12345")
    }

    @Test(expected = CodeInvalidException::class)
    fun testDenyCodeInvalid() {
        index.denyVerificationCode("12345")
    }
}
