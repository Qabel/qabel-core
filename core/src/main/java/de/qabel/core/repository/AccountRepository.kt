package de.qabel.core.repository

import de.qabel.core.config.Account
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

interface AccountRepository {
    /**
     * binds to Persistence specific UUID persistenceID. Use find(int id) instead or just findAll()
     */
    @Deprecated("")
    @Throws(EntityNotFoundException::class)
    fun find(id: String): Account

    @Throws(EntityNotFoundException::class)
    fun find(id: Int): Account

    @Throws(PersistenceException::class)
    fun findAll(): List<Account>

    @Throws(PersistenceException::class)
    fun save(account: Account)
}
