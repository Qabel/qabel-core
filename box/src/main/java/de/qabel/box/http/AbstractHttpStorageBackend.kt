package de.qabel.box.http

import de.qabel.core.logging.QabelLog
import org.apache.http.HttpRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import java.net.URI

open class AbstractHttpStorageBackend(root: String) : QabelLog {
    var httpclient: CloseableHttpClient
    protected var root: URI

    init {
        var root = root
        if (!root.endsWith("/")) {
            root += "/"
        }
        this.root = URI(root)

        // Increase max total connection
        val connManager = PoolingHttpClientConnectionManager()
        connManager.maxTotal = CONNECTIONS
        // Increase default max connection per route
        // Set to the max total because we only have 1 route
        connManager.defaultMaxPerRoute = CONNECTIONS

        httpclient = HttpClients.custom().disableContentCompression()    // workaround for nginx consuming ETags with gzip
                .setConnectionManager(connManager).build()
    }

    /**
     * This method can be overwritten to modify the request before it is executed.
     * You may want to add special headers here.
     */
    protected open fun prepareRequest(request: HttpRequest) {

    }

    companion object {
        // Number of http connections to S3
        // The default was too low, 20 works. Further testing may be required
        // to find the best amount of connections.
        protected val CONNECTIONS = 50
    }
}
