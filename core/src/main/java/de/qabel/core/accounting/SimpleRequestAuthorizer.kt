package de.qabel.core.accounting

import org.apache.http.HttpRequest


class SimpleRequestAuthorizer(private val authToken: String): RequestAuthorizer {
    override fun authorize(request: HttpRequest) {
        request.addHeader("Authorization", "Token " + authToken)
    }

}
