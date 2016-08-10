package de.qabel.core.index

import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine
import org.apache.http.message.BasicStatusLine


fun dummyStatusLine(statusCode: Int = 200): StatusLine {
    return BasicStatusLine(
        ProtocolVersion("HTTP", 1, 1),
        statusCode,
        "No good reason"
    )
}
