package de.qabel.core.accounting;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CloseableHttpResponseStub implements CloseableHttpResponse {
    public boolean closed;
    public StatusLine statusLine = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 0), 200, "OK");
    public HttpEntity entity;

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        statusLine = statusline;
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {

    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        statusLine = new BasicStatusLine(statusLine.getProtocolVersion(), code, "");
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {

    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return new Header[]{ new BasicHeader(name, headers.get(name)) };
    }

    @Override
    public Header getFirstHeader(String name) {
        return new BasicHeader(name, headers.get(name));
    }

    @Override
    public Header getLastHeader(String name) {
        return getFirstHeader(name);
    }

    @Override
    public Header[] getAllHeaders() {
        return new Header[0];
    }

    private Map<String, String> headers = new HashMap<>();

    @Override
    public void addHeader(Header header) {
        headers.put(header.getName(), header.getValue());
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void setHeader(Header header) {
        addHeader(header);
    }

    @Override
    public void setHeader(String name, String value) {
        addHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        for (Header header : headers) {
            setHeader(header);
        }
    }

    @Override
    public void removeHeader(Header header) {
        headers.remove(header.getName());
    }

    @Override
    public void removeHeaders(String name) {
        headers.remove(name);
    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(HttpParams params) {

    }
}
