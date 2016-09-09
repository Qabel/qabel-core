package de.qabel.core.index

import de.qabel.core.TestServer
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.junit.Before
import org.junit.Test
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.*

class IndexInteractorTest() : CoreTestCase {

    companion object {
        private val ALICE = RawContact("Alice", mutableListOf("0134531233"), mutableListOf("alice@qabel.de", "alice@al.com"), "1")
        private val BOB = RawContact("Bob", mutableListOf(), mutableListOf("bob@qabel.de", "bob@bo.com"), "2")
        private val RICHARD = RawContact("Richard X.", mutableListOf("2325446787"), mutableListOf("richard@xxx.de"), "3")
        private val EXTERNAL_CONTACTS: List<RawContact> = listOf(ALICE, BOB, RICHARD)
    }

    class MockContactsAccessor : ExternalContactsAccessor {
        override fun getContacts(): List<RawContact> = EXTERNAL_CONTACTS
    }

    lateinit var contactRepository: ContactRepository
    lateinit var identityRepository: IdentityRepository
    lateinit var indexServer: IndexServer
    lateinit var indexInteractor: IndexInteractor

    val exampleMail = "cindyB@mail.de"
    val examplePhone = "0123456711"
    private val exampleIdentity = createIdentity("Cindy").apply {
        email = exampleMail
        phone = examplePhone
    }

    private val identityAlice = createIdentity("Alice A").apply {
        email = ALICE.emailAddresses.first()
        phone = ALICE.mobilePhoneNumbers.first()
    }
    private val identityBob = createIdentity("Bob B.").apply {
        email = BOB.emailAddresses.first()
    }

    private val identities = listOf(exampleIdentity, identityAlice, identityBob)

    @Before
    fun setUp() {
        contactRepository = InMemoryContactRepository()
        identityRepository = InMemoryIdentityRepository().apply {
            save(exampleIdentity)
        }
        indexServer = IndexHTTP(IndexHTTPLocation(TestServer.INDEX))
        indexInteractor = MainIndexInteractor(indexServer, contactRepository, identityRepository)

        //Setup test contacts
        indexServer.updateIdentity(UpdateIdentity.fromIdentity(identityAlice, UpdateAction.CREATE))
        indexServer.updateIdentity(UpdateIdentity.fromIdentity(identityBob, UpdateAction.CREATE))
    }

    @After
    fun cleanUp() {
        identities.forEach {
            indexServer.updateIdentity(UpdateIdentity.fromIdentity(it, UpdateAction.DELETE))
        }
    }

    @Test
    fun testUpdateIdentity() {
        indexInteractor.updateIdentity(exampleIdentity)
        val resultMail = indexServer.searchForMail(exampleMail)
        assertThat(resultMail, hasSize(1))
        assertThat(resultMail.first().publicKey, equalTo(exampleIdentity.ecPublicKey))
    }

    @Test
    fun testUpdateIdentityPhone() {
        //With verified phone
        indexInteractor.updateIdentity(exampleIdentity)

        val oldPhone = exampleIdentity.phone
        exampleIdentity.phone = "9876543210"
        indexInteractor.updateIdentityPhone(exampleIdentity, oldPhone)
        assertThat(indexServer.searchForPhone(oldPhone), hasSize(0))
        val received = indexServer.searchForPhone(exampleIdentity.phone)
        assertThat(received, hasSize(1))
        assertThat(received.first().publicKey, equalTo(exampleIdentity.ecPublicKey))
    }

    @Test
    fun testUpdateIdentityEmail() {
        val oldMail = exampleIdentity.email
        exampleIdentity.email = "ab@c.de"
        indexInteractor.updateIdentityEmail(exampleIdentity, oldMail)
        assertThat(indexServer.searchForMail(oldMail), hasSize(0))
        val received = indexServer.searchForMail(exampleIdentity.email)
        assertThat(received, hasSize(1))
        assertThat(received.first().publicKey, equalTo(exampleIdentity.ecPublicKey))
    }

    @Test
    fun testDeleteIdentity() {
        indexInteractor.updateIdentity(exampleIdentity)

        val resultMail = indexServer.searchForMail(exampleMail)
        assertThat(resultMail, hasSize(1))

        indexInteractor.deleteIdentity(exampleIdentity)

        assertThat(indexServer.searchForMail(exampleMail), hasSize(0))
        assertThat(indexServer.searchForPhone(examplePhone), hasSize(0))
    }

    @Test
    fun testSync() {
        val externals = MockContactsAccessor()

        //We know bob, but without nick and optional data
        val contactBob = identityBob.toContact().apply {
            email = ""
            phone = ""
        }
        contactRepository.save(contactBob, exampleIdentity)

        val syncResults = indexInteractor.syncContacts(externals)
        assertThat(syncResults, hasSize(2))
        val aliceContact = syncResults.find { it.action == IndexSyncAction.CREATE }!!.contact
        assertEquals(aliceContact.nickName, ALICE.displayName)
        assertEquals(aliceContact.alias, identityAlice.alias)
        assertEquals(aliceContact.phone, identityAlice.phone)
        assertEquals(aliceContact.email, identityAlice.email)

        //Check Bob updated
        val bobContact = syncResults.find { it.action == IndexSyncAction.UPDATE }!!.contact
        assertEquals(bobContact.nickName, BOB.displayName)
        assertEquals(bobContact.alias, identityBob.alias)
        assertEquals(bobContact.phone, identityBob.phone)
        assertEquals(bobContact.email, identityBob.email)

        //Check no other contacts added
        assertThat(contactRepository.findWithIdentities(), hasSize(2))

        val reSyncResults = indexInteractor.syncContacts(externals)

        //Check resync dont updates anything
        assertThat(reSyncResults, hasSize(0))
    }

    @Test
    fun testSyncWithMultipleIdentities() {
        identityRepository.save(createIdentity("Dirk"))
        val syncResult = indexInteractor.syncContacts(MockContactsAccessor())
        assertThat(syncResult, hasSize(2))
        assert(syncResult.all { it.action == IndexSyncAction.CREATE })

        val storedContacts = contactRepository.findWithIdentities()
        assertThat(storedContacts, hasSize(2))
        assert(storedContacts.all { it.identities.isEmpty() })
    }

}
