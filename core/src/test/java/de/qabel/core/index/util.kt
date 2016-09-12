package de.qabel.core.index

import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine
import org.apache.http.message.BasicStatusLine
import java.util.*

private val random by lazy { Random() }

fun dummyStatusLine(statusCode: Int = 200): StatusLine {
    return BasicStatusLine(
        ProtocolVersion("HTTP", 1, 1),
        statusCode,
        "No good reason"
    )
}

fun randomMail(): String = "test-%s@example.net".format(UUID.randomUUID())

//Uses german cc + ndc
fun randomPhone(): String = "+491578" +
    mutableListOf<Int>().apply {
        while (size < 7) {
            var number = random.nextInt().mod(10)
            if (number < 0) {
                number = number.inv()
            }
            add(number)
        }
    }.joinToString("")

