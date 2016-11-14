package de.qabel.core.index.server

import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.IdentityStatus
import de.qabel.core.index.UpdateIdentity
import org.apache.http.client.methods.HttpUriRequest

internal interface IdentityStatusEndpoint : EndpointBase<IdentityStatus> {
    fun buildRequest(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): HttpUriRequest
}
