package de.qabel.core.accounting

import org.apache.http.HttpRequest

class DummyRequestAuthorizer(): RequestAuthorizer {
    override fun authorize(request: HttpRequest) { }
}


