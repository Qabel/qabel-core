package de.qabel.core.index.server

import org.apache.http.client.methods.HttpUriRequest

internal interface VerificationCodeEndpoint : EndpointBase<Unit> {
    fun buildRequest(code: String, confirm: Boolean): HttpUriRequest
}
