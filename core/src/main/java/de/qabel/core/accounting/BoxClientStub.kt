package de.qabel.core.accounting


import de.qabel.core.exceptions.QblCreateAccountFailException
import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.http.HttpRequest
import org.apache.http.client.utils.URIBuilder

import java.io.IOException
import java.util.ArrayList

class BoxClientStub : BoxClient {

    private var fixedQuotaState = QuotaState(1000000000L, 300000000L)

    var ioException: IOException? = null
    var qblInvalidCredentials: QblInvalidCredentials? = null
    var qblCreateAccountFailException: QblCreateAccountFailException? = null

    private fun maybeThrow() {
        ioException?.let {
            throw it
        }
        qblInvalidCredentials?.let{
            throw it
        }
    }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun login() = maybeThrow()

    override val quotaState: QuotaState
        @Throws(IOException::class, QblInvalidCredentials::class)
        get() {
            maybeThrow()
            return fixedQuotaState
        }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun authorize(request: HttpRequest) = maybeThrow()

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun updatePrefixes() = maybeThrow()

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun createPrefix() = maybeThrow()

    override fun buildUri(resource: String): URIBuilder = TODO()


    override fun buildBlockUri(resource: String): URIBuilder = TODO()

    override val prefixes: ArrayList<String>
        @Throws(IOException::class, QblInvalidCredentials::class)
        get() {
            maybeThrow()
            return ArrayList()
        }

    override val profile: AccountingProfile
        get() = TODO()

    @Throws(IOException::class)
    override fun resetPassword(email: String) = maybeThrow()

    @Throws(IOException::class, QblCreateAccountFailException::class)
    override fun createBoxAccount(email: String) {
        maybeThrow()
        qblCreateAccountFailException?.let {
            throw it
        }
    }
}
