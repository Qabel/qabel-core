package de.qabel.core.repository

import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.repository.EntityManager
import org.junit.Test

import java.net.URISyntaxException

import org.junit.Assert.*

class EntityManagerTest {
    private val em = EntityManager()

    @Test
    fun containsNothingOnStart() {
        assertFalse(em.contains(Identity::class.java, 1))
    }

    @Test
    @Throws(URISyntaxException::class)
    fun containsContainedEntity() {
        val identity = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("test").build()
        identity.id = 1
        em.put(Identity::class.java, identity)
        assertTrue(em.contains(Identity::class.java, 1))
    }

    @Test
    @Throws(Exception::class)
    fun containsNothingAfterClear() {
        val identity = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("test").build()
        identity.id = 1
        em.put(Identity::class.java, identity)
        em.clear()
        assertFalse(em.contains(Identity::class.java, 1))
    }
}
