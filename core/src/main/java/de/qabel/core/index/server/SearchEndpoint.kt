package de.qabel.core.index.server

import de.qabel.core.index.Field
import de.qabel.core.index.IndexContact
import org.apache.http.client.methods.HttpUriRequest

internal interface SearchEndpoint : EndpointBase<List<IndexContact>> {
    fun buildRequest(manyAttributes: List<Field>): HttpUriRequest
}
