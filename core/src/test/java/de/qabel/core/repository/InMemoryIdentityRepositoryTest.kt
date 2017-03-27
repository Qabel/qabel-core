package de.qabel.core.repository

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.junit.Assert.assertSame
import org.junit.Test

class InMemoryIdentityRepositoryTest {
    val repo = InMemoryIdentityRepository()
    val identityKey = QblECKeyPair()
    val identityName = "Identity"
    val identity = Identity(identityName, emptyList(), identityKey)

    @Test
    fun testSaveFind() {
        repo.save(identity)
        val result = repo.find(identity.id)
        assertSame(result, identity)
    }

    @Test(expected = EntityExistsException::class)
    fun saveDuplicateThrows() {
        repo.save(identity)
        identity.id = 0
        repo.save(identity)
    }
}
