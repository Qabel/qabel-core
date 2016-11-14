package de.qabel.core.index.server

import com.google.gson.Gson
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.*
import de.qabel.core.logging.QabelLog
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest


internal class DeleteIdentityEndpointImpl(
    val location: IndexHTTPLocation,
    val gson: Gson = createGson()
) : DeleteIdentityEndpoint, QabelLog {
    fun buildJsonRequest(identity: UpdateIdentity): String {
        return gson.toJson(EncryptedApiRequest(
            api = "delete-identity",
            timestamp = System.currentTimeMillis() / 1000
        ))
    }

    override fun buildRequest(identity: UpdateIdentity, serverPublicKey: QblECPublicKey): HttpUriRequest {
        val json = buildJsonRequest(identity)
        val uri = location.getUriBuilderForEndpoint("delete-identity").build()
        val request = HttpPost(uri)
        encryptJsonIntoRequest(json, identity.keyPair, serverPublicKey, request)
        return request
    }

    override fun parseResponse(jsonString: String, statusLine: StatusLine) {
    }
}
