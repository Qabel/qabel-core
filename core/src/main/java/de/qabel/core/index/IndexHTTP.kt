package de.qabel.core.index

import de.qabel.core.crypto.QblECPublicKey
import org.apache.http.HttpStatus
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

class IndexHTTP
internal constructor (
    val location: IndexHTTPLocation,
    private val httpClient: CloseableHttpClient = HttpClients.createMinimal(),
    private val key: ServerPublicKeyEndpoint = ServerPublicKeyEndpointImpl(location),
    private val search: SearchEndpoint = SearchEndpointImpl(location),
    private val update: UpdateEndpoint = UpdateEndpointImpl(location)
): IndexServer {
    constructor (location: IndexHTTPLocation, httpClient: CloseableHttpClient)
    : this(location, httpClient, ServerPublicKeyEndpointImpl(location))

    override fun search(attributes: Map<FieldType, String>): List<IndexContact> {
        val request = search.buildRequest(attributes)
        val response = httpClient.execute(request)
        return search.handleResponse(response)
    }

    override fun updateIdentity(identity: UpdateIdentity): UpdateResult {
        return updateIdentityWithRetries(identity)
    }

    fun updateIdentityWithRetries(identity: UpdateIdentity, retries: Int = 0): UpdateResult {
        val serverPublicKey = retrieveServerPublicKey()
        try {
            return updateIdentity(identity, serverPublicKey)
        } catch (e: APIError) {
            e.retries = retries
            if (e.code == HttpStatus.SC_BAD_REQUEST && retries < 2) {
                // a bad request / 400 may be caused by an outdated server public key, retry two times (total tries = 3)
                return updateIdentityWithRetries(identity, retries + 1)
            }
            throw e
        }
    }

    internal fun updateIdentity(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): UpdateResult {
        val request = update.buildRequest(identity, serverPublicKey)
        val response = httpClient.execute(request)
        return update.handleResponse(response)
    }

    internal fun retrieveServerPublicKey(): QblECPublicKey {
        val request = key.buildRequest()
        val response = httpClient.execute(request)
        return key.handleResponse(response)
    }
}
