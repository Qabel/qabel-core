package de.qabel.core.accounting;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CloseableHttpClientStub extends CloseableHttpClient {
    private boolean closed = false;
    private Map<String, CloseableHttpResponse> responses = new HashMap<>();
    private String body;

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        if (request instanceof HttpEntityEnclosingRequest) {
            InputStream contentStream = ((HttpEntityEnclosingRequest) request).getEntity().getContent();
            body = IOUtils.toString(contentStream);
        }
        String hash = hashRequest(request.getRequestLine().getMethod(), request.getRequestLine().getUri());
        if (!responses.containsKey(hash)) {
           throw new IllegalArgumentException("no response found for request'" + hash + "'");
        }
        return responses.get(hash);
    }

    public String getBody() {
        return body;
    }

    public void addResponse(String method, String uri, CloseableHttpResponse response) {
        responses.put(hashRequest(method, uri), response);
    }

    private String hashRequest(String method, String uri) {
        return method + " " + uri;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }

    public boolean isClosed() {
        return closed;
    }
}
