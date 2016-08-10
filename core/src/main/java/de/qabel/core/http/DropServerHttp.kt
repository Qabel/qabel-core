package de.qabel.core.http

import de.qabel.core.repository.entities.DropState
import java.net.URI

interface DropServerHttp {

    companion object {
        const val DEFAULT_AUTH_TOKEN = "Client Qabel"
    }

    object QblHeaders {
        const val AUTHORIZATION = "Authorization"
        const val X_QABEL_LATEST = "X-Qabel-Latest"
        const val X_QABEL_NEW_SINCE = "X-Qabel-New-Since"
    }

    object QblStatusCodes {
        const val OK = 200
        const val EMPTY_DROP = 204
        const val NOT_MODIFIED = 304
        const val INVALID = 400
        const val INVALID_SIZE = 413
    }

    data class DropServerResponse<T>(val statusCode: Int,
                                  val dropState: DropState,
                                  val dropMessages: List<T>)


    fun sendBytes(uri: URI, messageBytes: ByteArray)
    fun receiveMessageBytes(uri: URI, eTag: String): Triple<Int, String, Collection<ByteArray>>
}
