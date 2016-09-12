package de.qabel.core.index

import de.qabel.core.extensions.use
import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine
import org.apache.http.message.BasicStatusLine
import java.util.*


fun dummyStatusLine(statusCode: Int = 200): StatusLine {
    return BasicStatusLine(
        ProtocolVersion("HTTP", 1, 1),
        statusCode,
        "No good reason"
    )
}

fun randomMail() = "test-%s@example.net".format(UUID.randomUUID())
fun randomPhone() = "+49 1578 " + Random().ints(6).use { stream ->
    val values = mutableListOf<Int>()
    stream.forEach {
        values.add(it)
    }
    return values.joinToString("")
}

