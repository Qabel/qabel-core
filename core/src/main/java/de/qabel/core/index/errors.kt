package de.qabel.core.index

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.util.NoSuchElementException

open class IndexServerException @JvmOverloads constructor(message: String, cause: Throwable? = null): IOException(message, cause)

class MalformedResponseException @JvmOverloads constructor(message: String, cause: Throwable? = null): IndexServerException(message, cause)

class CodeInvalidException @JvmOverloads constructor(message: String, cause: Throwable? = null): IndexServerException(message, cause)

class CodeExpiredException @JvmOverloads constructor(message: String, cause: Throwable? = null): IndexServerException(message, cause)

class APIError(val code: Int, message: String, var retries: Int = 0): IndexServerException(message) {
    constructor(response: HttpResponse, retries: Int = 0)
    : this(
        code = response.statusLine.statusCode,
        message = "HTTP Status %d, %d retries, %s".format(response.statusLine.statusCode, retries, errorMessageFromResponse(response)),
        retries = retries)

    companion object {
        fun checkResponse(response: HttpResponse) {
            val statusCode = response.statusLine.statusCode
            if (!listOf(HttpStatus.SC_OK, HttpStatus.SC_ACCEPTED, HttpStatus.SC_NO_CONTENT).contains(statusCode)) {
                throw APIError(response)
            }
        }
    }
}


internal fun errorMessageFromResponse(response: HttpResponse): String {
    val json = try {
        EntityUtils.toString(response.entity)
    } catch (e: Exception) {
        return "No error message available (%s)".format(e)
    }
    return try {
        JsonParser().parse(json)["error"].string
    } catch (e: Exception) {
        var msg = "No error message available (%s)".format(e)
        if (e is JsonSyntaxException || e is NoSuchElementException) {
            msg += ", offending JSON: %s".format(json)
        }
        msg
    }
}
