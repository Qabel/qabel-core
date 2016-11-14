package de.qabel.core.index.server

import com.google.gson.Gson
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.*
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ByteArrayEntity

internal class UpdateEndpointImpl(
    val location: IndexHTTPLocation,
    val gson: Gson = createGson()
): UpdateEndpoint {

    internal data class UpdateRequest(
        val identity: IndexContact,
        val items: List<UpdateField>
    )

    fun buildJsonUpdateRequest(identity: UpdateIdentity): String {
        val updateRequest = UpdateRequest(
            identity = identity.toIndexContact(),
            items = identity.fields
        )
        return gson.toJson(updateRequest)
    }

    override fun buildRequest(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): HttpUriRequest {
        val json = buildJsonUpdateRequest(identity)
        val uri = location.getUriBuilderForEndpoint("update").build()
        val request = HttpPut(uri)
        encryptJsonIntoRequest(json, identity.keyPair, serverPublicKey, request)
        return request
    }

    override fun parseResponse(jsonString: String, statusLine: StatusLine): UpdateResult {
        val statusCode = statusLine.statusCode
        when(statusCode) {
            HttpStatus.SC_ACCEPTED -> return UpdateResult.ACCEPTED_DEFERRED
            HttpStatus.SC_NO_CONTENT -> return UpdateResult.ACCEPTED_IMMEDIATE
        }
        throw MalformedResponseException("Invalid status code on response (%d)".format(statusCode))
    }
}
