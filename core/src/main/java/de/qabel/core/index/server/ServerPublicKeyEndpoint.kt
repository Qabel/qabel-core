package de.qabel.core.index.server

import de.qabel.core.crypto.QblECPublicKey
import org.apache.http.client.methods.HttpUriRequest

internal interface ServerPublicKeyEndpoint : EndpointBase<QblECPublicKey> {
    fun buildRequest(): HttpUriRequest
}
