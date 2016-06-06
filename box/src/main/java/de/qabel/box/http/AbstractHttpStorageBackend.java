package de.qabel.box.http;

import org.apache.http.HttpRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class AbstractHttpStorageBackend {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractHttpStorageBackend.class);
    // Number of http connections to S3
    // The default was too low, 20 works. Further testing may be required
    // to find the best amount of connections.
    protected static final int CONNECTIONS = 50;
    protected final CloseableHttpClient httpclient;
    URI root;

    public AbstractHttpStorageBackend(String root) throws URISyntaxException {
        if (!root.endsWith("/")) {
            root += "/";
        }
        this.root = new URI(root);

        // Increase max total connection
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(CONNECTIONS);
        // Increase default max connection per route
        // Set to the max total because we only have 1 route
        connManager.setDefaultMaxPerRoute(CONNECTIONS);

        httpclient = HttpClients.custom()
            .disableContentCompression()    // workaround for nginx consuming ETags with gzip
            .setConnectionManager(connManager).build();
    }

    /**
     * This method can be overwritten to modify the request before it is executed.
     * You may want to add special headers here.
     */
    protected void prepareRequest(HttpRequest request) {

    }
}
