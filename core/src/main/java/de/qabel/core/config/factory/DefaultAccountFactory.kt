package de.qabel.core.config.factory

import de.qabel.core.config.Account

class DefaultAccountFactory : AccountFactory {
    override fun createAccount(provider: String, user: String, auth: String): Account {
        return Account(provider, user, auth)
    }
}
