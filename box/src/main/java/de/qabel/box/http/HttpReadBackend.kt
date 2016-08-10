package de.qabel.box.http

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.box.storage.StorageDownload
import de.qabel.box.storage.StorageReadBackend
import de.qabel.box.storage.UnmodifiedException
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet

import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException

open class HttpReadBackend @Throws(URISyntaxException::class)
constructor(root: String) : AbstractHttpStorageBackend(root), StorageReadBackend {

    @Throws(QblStorageException::class)
    override fun download(name: String): StorageDownload {
        try {
            return download(name, "")
        } catch (e: UnmodifiedException) {
            throw IllegalStateException(e)
        }

    }

    @Throws(QblStorageException::class, UnmodifiedException::class)
    override fun download(name: String, ifModifiedVersion: String?): StorageDownload {
        val uri = root.resolve(name)
        val httpGet = HttpGet(uri)
        if (ifModifiedVersion != null && ifModifiedVersion.isNotEmpty()) {
            httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, ifModifiedVersion)
        }
        prepareRequest(httpGet)

        try {
            val response = httpclient.execute(httpGet)
            try {
                val status = response.statusLine.statusCode
                if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_FORBIDDEN) {
                    throw QblStorageNotFound("File not found")
                }
                if (status == HttpStatus.SC_NOT_MODIFIED) {
                    throw UnmodifiedException()
                }
                if (status != HttpStatus.SC_OK) {
                    throw QblStorageException("Download error")
                }
                val modifiedVersion = response.getFirstHeader(HttpHeaders.ETAG).value

                if (modifiedVersion == ifModifiedVersion) {
                    throw UnmodifiedException()
                }
                val entity = response.entity ?: throw QblStorageException("No content")
                val content = entity.content
                return StorageDownload(content, modifiedVersion, entity.contentLength, response)
            } catch (e: Exception) {
                response.close()
                throw e
            }

        } catch (e: IOException) {
            throw QblStorageException(e)
        }

    }

    override fun getUrl(meta: String): String {
        return root.resolve(meta).toString()
    }
}
