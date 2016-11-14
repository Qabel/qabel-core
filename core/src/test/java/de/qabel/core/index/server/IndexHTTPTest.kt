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
import org.junit.Test
import java.util.*


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
        assertEquals(3, outdatedKey.numCalls)
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

    @Test
    fun testUpdateDropURL() {
        val testParts = UpdateTestParts(index)
        testParts.publishTest()

        val updatedIdentity = testParts.identity.copy(dropURL = DropURL("http://example.net/somewhere/abcdefghijklmnopqrstuvwxyzabcdefghijklmonop"))
        val updateResult = index.updateIdentity(updatedIdentity)
        assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, updateResult)

        val search1Result = index.search(mapOf(Pair(FieldType.EMAIL, testParts.mail)))
        assertEquals(1, search1Result.size)
        val foundPublicIdentity = search1Result[0]
        assertEquals(updatedIdentity.dropURL, foundPublicIdentity.dropUrl)
        assertEquals(updatedIdentity.alias, foundPublicIdentity.alias)
    }

    @Test
    fun testUpdateAlias() {
        val testParts = UpdateTestParts(index)
        testParts.publishTest()

        val updatedIdentity = testParts.identity.copy(alias = "1234")
        val updateResult = index.updateIdentity(updatedIdentity)
        assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, updateResult)

        val search1Result = index.search(mapOf(Pair(FieldType.EMAIL, testParts.mail)))
        assertEquals(1, search1Result.size)
        val foundPublicIdentity = search1Result[0]
        assertEquals(updatedIdentity.dropURL, foundPublicIdentity.dropUrl)
        assertEquals(updatedIdentity.alias, foundPublicIdentity.alias)
    }

    @Test
    fun testDeleteIdentity() {
        val testParts = UpdateTestParts(index)
        testParts.publishTest()

        val changedIdentity = testParts.identity.copy(alias = "1234", fields = listOf())
        index.deleteIdentity(changedIdentity)  // only the key matters

        val search1Result = index.search(mapOf(Pair(FieldType.EMAIL, testParts.mail)))
        assertEquals(0, search1Result.size)
    }

    private class UpdateTestParts(private val index: IndexHTTP) {
        val mail = getRandomMail()

        val identity = UpdateIdentity(
            keyPair = QblECKeyPair(),
            dropURL = DropURL("http://example.net/somewhere/abcdefghijklmnopqrstuvwxyzabcdefghijklmnopo"),
            alias = "Major Anya, Unicode: 裸共產主義",
            fields = listOf(
                UpdateField(UpdateAction.CREATE, FieldType.EMAIL, mail)
            )
        )

        fun publishTest() {
            val updateResult = index.updateIdentity(identity)
            /* Note that this only holds if the shallow verification facet is enabled, otherwise this would be
             * ACCEPTED_DEFERRED to check the email.
            */
            assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, updateResult)

            /* Will be found */
            searchForMailAndAssertOurs(mail)

            val status = index.identityStatus(identity)
            assertIdentityEquals(identity, status.identity)
            assertEquals(1, status.fieldStatus.size)
            val field = status.fieldStatus[0]
            assertEquals(EntryStatusEnum.CONFIRMED, field.status)
            assertEquals(FieldType.EMAIL, field.field)
            assertEquals(mail, field.value)
        }

        fun unpublishTest() {
            val identity = identity.copy(
                fields = listOf(
                    UpdateField(UpdateAction.DELETE, FieldType.EMAIL, mail)
                )
            )

            assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, index.updateIdentity(identity))

            assertEquals(0, index.search(mapOf(Pair(FieldType.EMAIL, mail))).size)  // gone
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
