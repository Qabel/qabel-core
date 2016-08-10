package de.qabel.core.accounting

import org.apache.http.*
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.message.BasicStatusLine
import org.apache.http.params.HttpParams

import java.io.IOException
import java.util.Locale

class CloseableHttpResponseStub : CloseableHttpResponse {
    override fun setEntity(entity: HttpEntity?) {
        this.privateEntity = entity
    }

    override fun getEntity(): HttpEntity {
        return this.privateEntity!!
    }

    var closed: Boolean = false
    private var statusLine: StatusLine = BasicStatusLine(ProtocolVersion("HTTP", 1, 0), 200, "OK")
    private var privateEntity: HttpEntity? = null

    @Throws(IOException::class)
    override fun close() {
        closed = true
    }

    override fun getStatusLine(): StatusLine {
        return statusLine
    }

    override fun setStatusLine(statusline: StatusLine) {
        statusLine = statusline
    }

    override fun setStatusLine(ver: ProtocolVersion, code: Int) {
    }

    override fun setStatusLine(ver: ProtocolVersion, code: Int, reason: String) {

    }

    @Throws(IllegalStateException::class)
    override fun setStatusCode(code: Int) {
        statusLine = BasicStatusLine(statusLine.protocolVersion, code, "")
    }

    @Throws(IllegalStateException::class)
    override fun setReasonPhrase(reason: String) {

    }

    override fun getLocale(): Locale? {
        return null
    }

    override fun setLocale(loc: Locale) {

    }

    override fun getProtocolVersion(): ProtocolVersion? {
        return null
    }

    override fun containsHeader(name: String): Boolean {
        return false
    }

    override fun getHeaders(name: String): Array<Header> {
        return arrayOfNulls<Int>(0) as Array<Header>
    }

    override fun getFirstHeader(name: String): Header? {
        return null
    }

    override fun getLastHeader(name: String): Header? {
        return null
    }

    override fun getAllHeaders(): Array<Header> {
        return arrayOfNulls<Int>(0) as Array<Header>
    }

    override fun addHeader(header: Header) {

    }

    override fun addHeader(name: String, value: String) {

    }

    override fun setHeader(header: Header) {

    }

    override fun setHeader(name: String, value: String) {

    }

    override fun setHeaders(headers: Array<Header>) {

    }

    override fun removeHeader(header: Header) {

    }

    override fun removeHeaders(name: String) {

    }

    override fun headerIterator(): HeaderIterator? {
        return null
    }

    override fun headerIterator(name: String): HeaderIterator? {
        return null
    }

    override fun getParams(): HttpParams? {
        return null
    }

    override fun setParams(params: HttpParams) {

    }
}
