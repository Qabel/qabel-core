package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertThat
import org.junit.Test

class InMemoryContactRepositoryTest {

    val repo = InMemoryContactRepository()

    @Test
    fun testSaveFind() {
        val identityKey = QblECKeyPair()
        val identityName = "Identity"

        val identity = Identity(identityName, emptyList(), identityKey)
        val contact = Contact("Test", emptyList(), QblECPublicKey("test".toByteArray()))

        repo.save(contact, identity)

        val sameIdentity = Identity(identityName, emptyList(), identityKey)

        val result = repo.find(sameIdentity)
        assertThat(result.contacts, hasSize(1))
    }

}
