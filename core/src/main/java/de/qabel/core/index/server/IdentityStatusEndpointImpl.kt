package de.qabel.core.index.server

import com.google.gson.Gson
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.*
import de.qabel.core.logging.QabelLog
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest


internal class IdentityStatusEndpointImpl(
    val location: IndexHTTPLocation,
    val gson: Gson = createGson()
) : IdentityStatusEndpoint, QabelLog {
    fun buildJsonRequest(identity: UpdateIdentity): String {
        return gson.toJson(EncryptedApiRequest(
            api = "status",
            timestamp = System.currentTimeMillis() / 1000
        ))
    }

    override fun buildRequest(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): HttpUriRequest {
        val json = buildJsonRequest(identity)
        val uri = location.getUriBuilderForEndpoint("status").build()
        val request = HttpPost(uri)
        encryptJsonIntoRequest(json, identity.keyPair, serverPublicKey, request)
        return request
    }

    private data class IdentityStatusResponse(
        val identity: IndexContact,
        val entries: List<EntryStatus>
    )

    override fun parseResponse(jsonString: String, statusLine: StatusLine): IdentityStatus {
        debug("Received identity status response: ${jsonString}")
        val response = gson.fromJson(jsonString, IdentityStatusResponse::class.java)
        return IdentityStatus(
            identity = response.identity,
            fieldStatus = response.entries
        )
    }
}
