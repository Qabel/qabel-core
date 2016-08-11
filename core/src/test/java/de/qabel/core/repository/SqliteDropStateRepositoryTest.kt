package de.qabel.core.repository

import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteDropStateRepository
import org.junit.Test

import org.junit.Assert.assertEquals

class SqliteDropStateRepositoryTest : AbstractSqliteRepositoryTest<SqliteDropStateRepository>() {

    @Throws(Exception::class)
    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): SqliteDropStateRepository {
        return SqliteDropStateRepository(clientDatabase, em)
    }

    @Test(expected = EntityNotFoundException::class)
    @Throws(Exception::class)
    fun throwsExceptionIfNoStateWasFound() {
        repo.getDropState("not existing")
    }

    @Test
    @Throws(Exception::class)
    fun knowsSavedDrops() {
        repo.setDropState("drop", "state")
        assertEquals("state", repo.getDropState("drop"))
    }

    @Test
    @Throws(Exception::class)
    fun replaceSavedDrops() {
        repo.setDropState("drop", "state")
        assertEquals("state", repo.getDropState("drop"))
        repo.setDropState("drop", "state2")
        assertEquals("state2", repo.getDropState("drop"))
    }


}
