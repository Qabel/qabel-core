package de.qabel.core.index.server

import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.UpdateIdentity
import de.qabel.core.index.UpdateResult
import org.apache.http.client.methods.HttpUriRequest

internal interface UpdateEndpoint : EndpointBase<UpdateResult> {
    fun buildRequest(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): HttpUriRequest
}
