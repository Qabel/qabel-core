package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Account
import de.qabel.core.config.factory.AccountFactory
import de.qabel.core.repository.EntityManager

import java.sql.ResultSet
import java.sql.SQLException

class AccountHydrator(private val em: EntityManager, private val accountFactory: AccountFactory) : AbstractHydrator<Account>() {

    override fun getFields(): Array<String> = arrayOf("id", "provider", "user", "auth", "token")

    @Throws(SQLException::class)
    override fun hydrateOne(resultSet: ResultSet): Account {
        var i = 1
        val id = resultSet.getInt(i++)

        if (em.contains(Account::class.java, id)) {
            return em.get(Account::class.java, id)
        }

        val provider = resultSet.getString(i++)
        val user = resultSet.getString(i++)
        val auth = resultSet.getString(i++)
        val token = resultSet.getString(i++)

        val account = accountFactory.createAccount(provider, user, auth)
        account.id = id
        account.token = token
        return account
    }

    override fun recognize(instance: Account) {
        em.put(Account::class.java, instance)
    }
}
