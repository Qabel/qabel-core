package de.qabel.core.accounting

import de.qabel.core.exceptions.QblCreateAccountFailException
import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.http.HttpRequest
import org.apache.http.client.utils.URIBuilder

import java.io.IOException
import java.util.ArrayList

interface BoxClient {


    @Throws(IOException::class, QblInvalidCredentials::class)
    fun login()

    val quotaState: QuotaState

    @Throws(IOException::class, QblInvalidCredentials::class)
    fun authorize(request: HttpRequest)

    @Throws(IOException::class, QblInvalidCredentials::class)
    fun updatePrefixes()

    @Throws(IOException::class, QblInvalidCredentials::class)
    fun createPrefix()

    fun buildUri(resource: String): URIBuilder

    fun buildBlockUri(resource: String): URIBuilder

    val prefixes: ArrayList<String>

    val profile: AccountingProfile

    @Throws(IOException::class)
    fun resetPassword(email: String)

    @Throws(IOException::class, QblCreateAccountFailException::class)
    fun createBoxAccount(email: String)
}
