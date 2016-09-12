package de.qabel.core.index.server

import de.qabel.core.index.FieldType
import de.qabel.core.index.IndexContact
import org.apache.http.client.methods.HttpUriRequest

internal interface SearchEndpoint : EndpointBase<List<IndexContact>> {
    fun buildRequest(attributes: Map<FieldType, String>): HttpUriRequest
}
