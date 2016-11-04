package de.qabel.core.repository.sqlite

import de.qabel.core.config.Account
import de.qabel.core.config.factory.DefaultAccountFactory
import de.qabel.core.repository.AccountRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.hydrator.AccountHydrator
import java.sql.SQLException

class SqliteAccountRepository(database: ClientDatabase, hydrator: Hydrator<Account>)
    : AbstractSqliteRepository<Account>(database, hydrator, SqliteAccountRepository.TABLE_NAME), AccountRepository {

    constructor(clientDatabase: ClientDatabase, em: EntityManager)
    : this(clientDatabase, AccountHydrator(em, DefaultAccountFactory())) { }

    @Throws(EntityNotFoundException::class)
    override fun find(id: String): Account {
        try {
            return super.findBy("id=?", id)
        } catch (e: PersistenceException) {
            throw EntityNotFoundException("no account with id " + id, e)
        }

    }

    @Throws(EntityNotFoundException::class)
    override fun find(id: Int): Account = find(id.toString())

    @Throws(PersistenceException::class)
    override fun findAll(): List<Account> = super.findAll("").toList()

    @Throws(PersistenceException::class)
    override fun save(account: Account) {
        try {
            val loaded = findBy("`provider` = ? AND `user` = ?", account.provider, account.user)

            try {
                database.prepare(
                        "UPDATE `account` SET `auth` = ?, `token` = ? WHERE ROWID = ?").use { statement ->
                    statement.setString(1, account.auth)
                    statement.setString(2, account.token)
                    statement.setInt(3, loaded.id)
                    statement.execute()
                    account.id = loaded.id
                }
            } catch (e: SQLException) {
                throw PersistenceException("failed to update account", e)
            }

            return
        } catch (ignored: EntityNotFoundException) {
        }

        try {
            database.prepare(
                    "INSERT INTO `account` (`provider`, `user`, `auth`, `token`) VALUES (?, ?, ?, ?)").use { statement ->
                var i = 1
                statement.setString(i++, account.provider)
                statement.setString(i++, account.user)
                statement.setString(i++, account.auth)
                statement.setString(i++, account.token)
                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    keys.next()
                    account.id = keys.getInt(1)
                }

                hydrator.recognize(account)
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed to save account " + account.user + "@" + account.provider, e)
        }

    }

    companion object {
        @JvmStatic val TABLE_NAME = "account"
    }
}
