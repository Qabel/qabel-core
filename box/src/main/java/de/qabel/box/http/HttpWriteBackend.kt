package de.qabel.box.http

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.box.storage.StorageWriteBackend
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.InputStreamEntity

import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

open class HttpWriteBackend @Throws(URISyntaxException::class)
constructor(root: String) : AbstractHttpStorageBackend(root), StorageWriteBackend {

    @Throws(QblStorageException::class)
    override fun upload(name: String, content: InputStream): Long {
        val httpPost: HttpPost
        try {
            val uri = root.resolve(name)
            httpPost = HttpPost(uri)
            prepareRequest(httpPost)
            httpPost.entity = InputStreamEntity(content)

            httpclient.execute(httpPost).use { response ->
                val status = response.statusLine.statusCode
                if (status == 404 || status == 403) {
                    throw QblStorageNotFound("File not found")
                }
                if (status >= 300) {
                    throw QblStorageException("Upload error")
                }
            }
            return System.currentTimeMillis()
        } catch (e: IOException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun delete(name: String) {
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
            throw QblStorageNotFound("File not found")
        }
        if (status >= 300) {
            throw QblStorageException("Deletion error")
        }
    }
}
