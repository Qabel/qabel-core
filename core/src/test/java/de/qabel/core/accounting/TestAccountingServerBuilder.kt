package de.qabel.core.accounting

import de.qabel.core.config.AccountingServer

import java.net.URI
import java.net.URISyntaxException

class TestAccountingServerBuilder @Throws(URISyntaxException::class)
constructor() {
    private var accountingUri: URI? = null
    private var blockUri: URI? = null
    private var user = "testuser"
    private var pass = DEFAULT_PASS

    init {
        accountingUri = URI("http://localhost:9696")
        blockUri = URI("http://localhost:9697")
    }

    fun user(user: String): TestAccountingServerBuilder {
        this.user = user
        if (pass == DEFAULT_PASS) {
            pass = "randomPass"
        }
        return this
    }

    fun pass(pass: String): TestAccountingServerBuilder {
        this.pass = pass
        return this
    }

    fun block(blockUri: URI): TestAccountingServerBuilder {
        this.blockUri = blockUri
        return this
    }

    fun accounting(accountingUri: URI): TestAccountingServerBuilder {
        this.accountingUri = accountingUri
        return this
    }

    fun build(): AccountingServer {
        return AccountingServer(
                accountingUri,
                blockUri!!,
                user,
                pass)
    }

    companion object {
        val DEFAULT_PASS = "testuser"
    }
}
