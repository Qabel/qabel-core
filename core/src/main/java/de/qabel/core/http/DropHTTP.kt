package de.qabel.core.http

import org.apache.commons.io.IOUtils
import org.apache.http.client.utils.DateUtils
import org.apache.james.mime4j.MimeException
import org.apache.james.mime4j.stream.EntityState
import org.apache.james.mime4j.stream.MimeTokenStream

import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLConnection
import java.text.ParseException
import java.util.ArrayList
import java.util.Date


class DropHTTP {

    fun send(uri: URI, message: ByteArray): HTTPResult<*> {
        val result = HTTPResult<ByteArray>()
        val conn = setupConnection(uri) as HttpURLConnection
        conn.doOutput = true // indicates POST method
        conn.doInput = true
        conn.setRequestProperty("Content-Type", "application/octet-stream")
        conn.setRequestProperty("Authorization", "Client Qabel")

        // conn.setFixedLengthStreamingMode();
        val out: DataOutputStream
        try {
            out = DataOutputStream(conn.outputStream)
            out.write(message)
            out.flush()
            out.close()
            result.responseCode = conn.responseCode
            result.isOk = conn.responseCode == 200
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } finally {
            conn.disconnect()

        }
        return result
    }

    @Throws(IOException::class)
    @JvmOverloads fun receiveMessages(uri: URI, sinceDate: Long = 0): HTTPResult<Collection<ByteArray>> {
        val result = HTTPResult<Collection<ByteArray>>()
        val conn = setupConnection(uri) as HttpURLConnection
        conn.ifModifiedSince = sinceDate
        val messages = ArrayList<ByteArray>()
        try {
            conn.requestMethod = "GET"
            result.responseCode = conn.responseCode
            result.isOk = conn.responseCode == 200
            if (result.isOk) {
                if (conn.getHeaderField("Last-Modified") != null) {
                    try {
                        result.setLastModified(parseDate(conn.getHeaderField("Last-Modified")))
                    } catch (ignored: ParseException) {
                    }

                }
                val inputstream = conn.inputStream
                val stream = MimeTokenStream()
                stream.parseHeadless(inputstream, conn.contentType)
                var state = stream.state
                while (state != EntityState.T_END_OF_STREAM) {
                    if (state == EntityState.T_BODY) {
                        val message = IOUtils.toByteArray(stream.inputStream)
                        messages.add(message)
                    }
                    state = stream.next()
                }
            }
        } catch (e: MimeException) {
            throw IllegalStateException("error while parsing mime response: " + e.message, e)
        } finally {
            conn.disconnect()
        }
        result.data = messages
        return result
    }

    @Throws(ParseException::class)
    private fun parseDate(dateHeader: String): Date {
        return DateUtils.parseDate(dateHeader)
    }

    @Throws(IOException::class)
    @JvmOverloads fun head(uri: URI, sinceDate: Long = 0): HTTPResult<*> {
        val result = HTTPResult<ByteArray>()
        val conn = setupConnection(uri) as HttpURLConnection
        conn.ifModifiedSince = sinceDate
        try {
            conn.requestMethod = "GET"
            result.responseCode = conn.responseCode
            result.isOk = conn.responseCode == 200
        } finally {
            conn.disconnect()
        }
        return result
    }

    private fun setupConnection(uri: URI): URLConnection = uri.toURL().openConnection()
}
