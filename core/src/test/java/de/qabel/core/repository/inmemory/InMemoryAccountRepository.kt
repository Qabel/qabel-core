package de.qabel.core.repository.inmemory

import de.qabel.core.config.Account
import de.qabel.core.repository.AccountRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

import java.util.LinkedList

class InMemoryAccountRepository : AccountRepository {
    private val accounts = LinkedList<Account>()

    @Throws(EntityNotFoundException::class)
    override fun find(id: String): Account {
        for (account in accounts) {
            if (id == account.id.toString()) {
                return account
            }
        }
        throw EntityNotFoundException("no account for id " + id)
    }

    @Throws(EntityNotFoundException::class)
    override fun find(id: Int): Account {
        return find(id.toString())
    }

    @Throws(PersistenceException::class)
    override fun findAll(): List<Account> {
        return accounts
    }

    @Throws(PersistenceException::class)
    override fun save(account: Account) {
        if (account.id == 0) {
            account.id = accounts.size + 1
        }
        if (!accounts.contains(account)) {
            accounts.add(account)
        }
    }
}
