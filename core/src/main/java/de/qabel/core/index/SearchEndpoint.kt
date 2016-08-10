package de.qabel.core.index

import org.apache.http.client.methods.HttpUriRequest

internal interface SearchEndpoint : EndpointBase<List<IndexContact>> {
    fun buildRequest(attributes: Map<FieldType, String>): HttpUriRequest
}
