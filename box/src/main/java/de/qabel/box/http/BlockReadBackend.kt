package de.qabel.box.http

import de.qabel.core.accounting.BoxClient
import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.http.HttpRequest

import java.io.IOException
import java.net.URISyntaxException

class BlockReadBackend @Throws(URISyntaxException::class)
constructor(root: String, private val accountingHTTP: BoxClient) : HttpReadBackend(root) {

    override fun prepareRequest(request: HttpRequest) {
        super.prepareRequest(request)
        try {
            accountingHTTP.authorize(request)
        } catch (e: IOException) {
            throw IllegalStateException("failed to authorize block request: " + e.message, e)
        } catch (e: QblInvalidCredentials) {
            throw IllegalStateException("failed to authorize block request: " + e.message, e)
        }

    }
}
