package de.qabel.core.accounting

import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.http.HttpRequest
import java.io.IOException


interface RequestAuthorizer {

    @Throws(IOException::class, QblInvalidCredentials::class)
    fun authorize(request: HttpRequest)
}
