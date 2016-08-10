package de.qabel.core.index

import de.qabel.core.crypto.QblECPublicKey
import org.apache.http.client.methods.HttpUriRequest

internal interface UpdateEndpoint : EndpointBase<UpdateResult> {
    fun buildRequest(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): HttpUriRequest
}
