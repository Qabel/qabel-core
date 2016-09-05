package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.EntityObserver
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.junit.Assert.*

class InMemoryContactRepositoryTest {

    val repo = InMemoryContactRepository()
    var hasCalled: Boolean = false
    val identityKey = QblECKeyPair()
    val identityName = "Identity"
    val identity = createIdentity(identityName, identityKey)
    val contact = createContact()

    @Test
    fun testSaveFind() {
        repo.save(contact, identity)
        val sameIdentity = Identity(identityName, emptyList(), identityKey)
        val result = repo.find(sameIdentity)
        assertThat(result.contacts, hasSize(1))
    }

    @Test(expected = EntityExistsException::class)
    fun saveDuplicateContactThrowsException() {
        repo.save(contact, identity)
        val contactDuplicate = createContact()
        repo.save(contactDuplicate, identity)
    }

    private fun createContact(): Contact {
        return Contact("Test", emptyList(), QblECPublicKey("test".toByteArray()))
    }

    private fun createIdentity(identityName: String, identityKey: QblECKeyPair): Identity {
        return Identity(identityName, emptyList(), identityKey)
    }

    private fun attachEntityObserver() {
        repo.attach(EntityObserver { hasCalled = true })
    }

    @Test
    fun testContactRepositorySaveObservable() {
        attachEntityObserver()
        repo.save(contact, identity)
        assertTrue(hasCalled)
    }

    @Test
    fun testContactRepositoryDeleteObservable() {
        attachEntityObserver()
        repo.delete(contact, identity)
        assertTrue(hasCalled)
    }
}
