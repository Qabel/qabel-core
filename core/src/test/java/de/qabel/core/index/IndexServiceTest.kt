package de.qabel.core.index

import de.qabel.core.TestServer
import de.qabel.core.config.Identity
import de.qabel.core.config.VerificationStatus
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.copy
import de.qabel.core.extensions.createIdentity
import de.qabel.core.index.server.ExternalContactsAccessor
import de.qabel.core.index.server.IndexHTTP
import de.qabel.core.index.server.IndexHTTPLocation
import de.qabel.core.index.server.IndexServer
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.junit.Before
import org.junit.Test
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.*
import com.nhaarman.mockito_kotlin.*
import org.mockito.Mockito

open class IndexServiceTest() : CoreTestCase {

    companion object {
        private val ALICE = RawContact("Alice", mutableListOf(randomPhone()), mutableListOf(randomMail(), randomMail()), "1")
        private val BOB = RawContact("Bob", mutableListOf(), mutableListOf(randomMail(), randomMail()), "2")
        private val RICHARD = RawContact("Richard X.", mutableListOf(randomPhone()), mutableListOf(randomMail()), "3")
        private val EXTERNAL_CONTACTS: List<RawContact> = listOf(ALICE, BOB, RICHARD)
    }

    class MockContactsAccessor : ExternalContactsAccessor {
        override fun getContacts(): List<RawContact> = EXTERNAL_CONTACTS
    }

    open val testServerLocation: IndexHTTPLocation by lazy { IndexHTTPLocation(TestServer.INDEX) }

    lateinit var contactRepository: ContactRepository
    lateinit var identityRepository: IdentityRepository
    lateinit var indexServer: IndexServer
    lateinit var indexService: IndexService

    val exampleMail = randomMail()
    val examplePhone = randomPhone()

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

    fun matchIndexResult(result: List<IndexContact>, vararg identities: Identity) {
        assertThat(result, hasSize(identities.size))
        identities.forEach { identity ->
            assert(result.any {
                it.alias == identity.alias && it.publicKey.readableKeyIdentifier == identity.keyIdentifier
            })
        }
    }

    @Before
    fun setUp() {
        contactRepository = InMemoryContactRepository()
        identityRepository = InMemoryIdentityRepository().apply {
            save(exampleIdentity)
        }
        indexServer = IndexHTTP(testServerLocation)
        indexService = MainIndexService(indexServer, contactRepository, identityRepository)

        //Setup test contacts
        indexServer.updateIdentity(UpdateIdentity.fromIdentity(identityAlice, UpdateAction.CREATE))
        indexServer.updateIdentity(UpdateIdentity.fromIdentity(identityBob, UpdateAction.CREATE))

        MockitoKotlin.registerInstanceCreator { UpdateIdentity(exampleIdentity, emptyList()) }
    }

    @After
    fun cleanUp() {
        identities.forEach {
            indexServer.updateIdentity(UpdateIdentity.fromIdentity(it, UpdateAction.DELETE))
        }
    }

    @Test
    fun testUpdateIdentity() {
        indexService.updateIdentity(exampleIdentity)
        val resultMail = indexServer.searchForMail(exampleMail)
        matchIndexResult(resultMail, exampleIdentity)
        val resultPhone = indexServer.searchForPhone(examplePhone)
        matchIndexResult(resultPhone, exampleIdentity)
    }

    @Test
    fun testUpdateIdentityAlias() {
        indexService.updateIdentity(exampleIdentity)

        val changedIdentity = copy(exampleIdentity).apply {
            alias = "ChangedAlias"
        }
        indexService.updateIdentity(changedIdentity, exampleIdentity)
        val changedResult = indexServer.searchForMail(exampleMail)
        matchIndexResult(changedResult, changedIdentity)
    }

    @Test
    fun testUpdateIdentityPhone() {
        indexService.updateIdentity(exampleIdentity)
        val changedIdentity = copy(exampleIdentity)
        val newPhone = randomPhone()
        changedIdentity.phone = newPhone

        indexService.updateIdentity(changedIdentity, exampleIdentity)
        matchIndexResult(indexServer.searchForPhone(newPhone), changedIdentity)

        //Check old phone removed
        matchIndexResult(indexServer.searchForPhone(examplePhone))
    }

    @Test
    fun testUpdateIdentityEmail() {
        indexService.updateIdentity(exampleIdentity)
        val changedIdentity = copy(exampleIdentity)
        val newMail = randomMail()
        changedIdentity.email = newMail

        indexService.updateIdentity(changedIdentity, exampleIdentity)

        val resultMail = indexServer.searchForMail(newMail)
        matchIndexResult(resultMail, changedIdentity)

        //Check old mail removed
        matchIndexResult(indexServer.searchForMail(exampleMail))
    }

    @Test
    fun testRemoveIdentity() {
        indexService.updateIdentity(exampleIdentity)

        val resultMail = indexServer.searchForMail(exampleMail)
        assertThat(resultMail, hasSize(1))

        indexService.removeIdentity(exampleIdentity)

        assertThat(indexServer.searchForMail(exampleMail), hasSize(0))
        assertThat(indexServer.searchForPhone(examplePhone), hasSize(0))
    }

    @Test
    fun testRemoveIdentityWithoutFields() {
        val newIdentity = createIdentity("Private")
        indexService.removeIdentity(newIdentity)
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

        val syncResults = indexService.syncContacts(externals)
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

        val reSyncResults = indexService.syncContacts(externals)

        //Check resync dont updates anything
        assertThat(reSyncResults, hasSize(0))
    }

    @Test
    fun testSyncWithMultipleIdentities() {
        identityRepository.save(createIdentity("Dirk"))
        val syncResult = indexService.syncContacts(MockContactsAccessor())
        assertThat(syncResult, hasSize(2))
        assert(syncResult.all { it.action == IndexSyncAction.CREATE })

        val storedContacts = contactRepository.findWithIdentities()
        assertThat(storedContacts, hasSize(2))
        assert(storedContacts.all { it.identities.isEmpty() })
    }

    @Test
    fun testSearchMail() {
        val results = indexService.searchContacts(identityAlice.email, "")
        assertThat(results, hasSize(1))
        val found = results.first()
        assertThat(found.keyIdentifier, equalTo(identityAlice.keyIdentifier))
        assertThat(found.alias, equalTo(identityAlice.alias))
        assertThat(found.email, equalTo(identityAlice.email))
        assert(found.phone.isNullOrBlank())
    }

    @Test
    fun testSearchPhone() {
        val results = indexService.searchContacts("", identityAlice.phone)
        assertThat(results, hasSize(1))
        val found = results.first()
        assertThat(found.keyIdentifier, equalTo(identityAlice.keyIdentifier))
        assertThat(found.alias, equalTo(identityAlice.alias))
        assertThat(found.phone, equalTo(identityAlice.phone))
        assert(found.email.isNullOrBlank())
    }

    @Test
    fun testSearch() {
        val results = indexService.searchContacts(identityAlice.email, identityAlice.phone)
        assertThat(results, hasSize(1))
        val found = results.first()
        assertThat(found.keyIdentifier, equalTo(identityAlice.keyIdentifier))
        assertThat(found.alias, equalTo(identityAlice.alias))
        assertThat(found.email, equalTo(identityAlice.email))
        assertThat(found.phone, equalTo(identityAlice.phone))
    }

    @Test
    fun testUpdateIdentities() {
        identityRepository.save(identityAlice)

        indexService.updateIdentities()

        val resultAlice = indexServer.searchForMail(identityAlice.email)
        matchIndexResult(resultAlice, identityAlice)

        val resultExample = indexServer.searchForPhone(examplePhone)
        matchIndexResult(resultExample, exampleIdentity)
    }

    @Test
    fun testRemoveIdentities() {
        identityRepository.save(identityAlice)
        indexService.updateIdentity(exampleIdentity)
        indexService.updateIdentity(identityAlice)

        indexService.removeIdentities()

        val resultAlice = indexServer.searchForMail(identityAlice.email)
        matchIndexResult(resultAlice)
        assertThat(identityAlice.emailStatus, equalTo(VerificationStatus.NOT_VERIFIED))
        assertThat(identityAlice.phoneStatus, equalTo(VerificationStatus.NOT_VERIFIED))

        val resultExample = indexServer.searchForPhone(examplePhone)
        matchIndexResult(resultExample)
        assertThat(exampleIdentity.emailStatus, equalTo(VerificationStatus.NOT_VERIFIED))
        assertThat(exampleIdentity.phoneStatus, equalTo(VerificationStatus.NOT_VERIFIED))
    }

    @Test
    fun testFieldNotChanged() {
        val indexServer: IndexServer = mock()
        whenever(indexServer.updateIdentity(any())).thenReturn(UpdateResult.ACCEPTED_IMMEDIATE)

        val indexService = MainIndexService(indexServer, contactRepository, identityRepository)

        val update = UpdateIdentity(exampleIdentity, listOf(UpdateField(UpdateAction.DELETE, FieldType.EMAIL, exampleMail)))
        val notChangedCopy = copy(exampleIdentity)
        indexService.updateIdentity(exampleIdentity, notChangedCopy)
        verify(indexServer, never()).updateIdentity(update)
        reset(indexServer)
        whenever(indexServer.updateIdentity(any())).thenReturn(UpdateResult.ACCEPTED_IMMEDIATE)

        exampleIdentity.email = "test@test.de"
        indexService.updateIdentity(exampleIdentity, notChangedCopy)
        verify(indexServer, times(2)).updateIdentity(any())

    }

}
