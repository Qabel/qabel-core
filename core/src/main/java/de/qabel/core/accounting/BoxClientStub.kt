package de.qabel.core.accounting


import de.qabel.core.exceptions.QblCreateAccountFailException
import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.http.HttpRequest
import org.apache.http.client.utils.URIBuilder

import java.io.IOException
import java.util.ArrayList

class BoxClientStub : BoxClient {

    var quotaState = QuotaState(1000000000L, 300000000L)

    var ioException: IOException? = null
    var qblInvalidCredentials: QblInvalidCredentials? = null
    var qblCreateAccountFailException: QblCreateAccountFailException? = null

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun login() {
        if (ioException != null) {
            throw ioException
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials
        }
    }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun getQuotaState(): QuotaState {
        if (ioException != null) {
            throw ioException
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials
        }
        return quotaState
    }


    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun authorize(request: HttpRequest) {
        if (ioException != null) {
            throw ioException
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials
        }
    }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun updatePrefixes() {
        if (ioException != null) {
            throw ioException
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials
        }

    }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun createPrefix() {
        if (ioException != null) {
            throw ioException
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials
        }
    }

    override fun buildUri(resource: String): URIBuilder? {
        return null
    }


    override fun buildBlockUri(resource: String): URIBuilder? {
        return null
    }

    override val prefixes: ArrayList<String>?
        @Throws(IOException::class, QblInvalidCredentials::class)
        get() {
            if (ioException != null) {
                throw ioException
            }
            if (qblInvalidCredentials != null) {
                throw qblInvalidCredentials
            }
            return null
        }

    override val profile: AccountingProfile?
        get() = null

    @Throws(IOException::class)
    override fun resetPassword(email: String) {

    }

    @Throws(IOException::class, QblCreateAccountFailException::class)
    override fun createBoxAccount(email: String) {
        if (ioException != null) {
            throw ioException
        }
        if (qblCreateAccountFailException != null) {
            throw qblCreateAccountFailException
        }
    }
}
