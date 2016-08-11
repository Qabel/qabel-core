package de.qabel.core.config.factory

import de.qabel.core.config.Account

interface AccountFactory {
    fun createAccount(provider: String, user: String, auth: String): Account
}
