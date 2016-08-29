package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.ContactObserver
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.hamcrest.Matchers.hasSize
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class InMemoryContactRepositoryTest {

    val repo = InMemoryContactRepository()
    val hasCalled = AtomicBoolean()
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

    private fun attachContactObserver() {
        repo.attach(ContactObserver { hasCalled.set(true) })
    }

    @Test
    @Throws(Exception::class)
    fun testContactRepositorySaveObservable() {
        attachContactObserver()
        repo.save(contact, identity)
        Assert.assertTrue(hasCalled.get())
    }

    @Test
    @Throws(Exception::class)
    fun testContactRepositoryDeleteObservable() {
        attachContactObserver()
        repo.delete(contact, identity)
        Assert.assertTrue(hasCalled.get())
    }
}
