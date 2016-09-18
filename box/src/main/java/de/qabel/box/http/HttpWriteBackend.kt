package de.qabel.box.http

import de.qabel.box.storage.ModifiedException
import de.qabel.box.storage.StorageWriteBackend
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.logging.QabelLog
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.DateUtils
import org.apache.http.entity.InputStreamEntity
import sun.plugin.dom.exception.InvalidStateException
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

open class HttpWriteBackend @Throws(URISyntaxException::class)
constructor(root: String) : AbstractHttpStorageBackend(root), StorageWriteBackend, QabelLog {

    @Throws(QblStorageException::class)
    override fun upload(name: String, content: InputStream) = uploadIfOld(name, content, null)

    @Throws(QblStorageException::class)
    override fun upload(name: String, content: InputStream, eTag: String?) = uploadIfOld(name, content, eTag)

    @Throws(QblStorageException::class)
    fun uploadIfOld(name: String, content: InputStream, eTag: String?): StorageWriteBackend.UploadResult {
        trace("Uploading " + name)
        val httpPost: HttpPost
        try {
            val uri = root.resolve(name)
            httpPost = HttpPost(uri)
            prepareRequest(httpPost)
            if (eTag != null) httpPost.addHeader("If-Match", eTag)
            httpPost.entity = InputStreamEntity(content)

            httpclient.execute(httpPost).use { response ->
                val status = response.statusLine.statusCode
                if (status == 412) {
                    throw ModifiedException("The target file was already changed: ${statusToMessage(response)}")
                } else if (status == 404 || status == 403) {
                    throw QblStorageNotFound("File not found: ${statusToMessage(response)}")
                } else if (status >= 300) {
                    throw QblStorageException("Upload error: ${statusToMessage(response)}")
                }
                return parseUploadResult(response)
            }
        } catch (e: IOException) {
            throw QblStorageException(e)
        }

    }

    private fun statusToMessage(response: CloseableHttpResponse): String = with (response.statusLine) {
        return "${statusCode} '${reasonPhrase}'"
    }

    private fun parseUploadResult(response: CloseableHttpResponse): StorageWriteBackend.UploadResult {
        checkHeader("Date", response)
        checkHeader("Etag", response)
        val time = DateUtils.parseDate(response.getFirstHeader("Date").value)
        val etag = response.getFirstHeader("Etag").value
        return StorageWriteBackend.UploadResult(time, etag)
    }

    private fun checkHeader(headerName: String, response: CloseableHttpResponse) {
        if (!response.containsHeader(headerName))
            throw InvalidStateException("Missing $headerName header on block server response")
    }

    @Throws(QblStorageException::class)
    override fun delete(name: String) {
        trace("Deleting " + name)
        val uri: URI
        val response: CloseableHttpResponse

        try {
            uri = root.resolve(name)
            val httpDelete = HttpDelete(uri)
            prepareRequest(httpDelete)

            response = httpclient.execute(httpDelete)
        } catch (e: IOException) {
            throw QblStorageException(e)
        }

        val status = response.statusLine.statusCode
        if (status == 404 || status == 403) {
            throw QblStorageNotFound("File not found: ${statusToMessage(response)}")
        }
        if (status >= 300) {
            throw QblStorageException("Deletion error: ${statusToMessage(response)}")
        }
    }
}
