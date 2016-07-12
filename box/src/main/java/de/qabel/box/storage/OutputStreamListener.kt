package de.qabel.box.storage

import java.io.FilterOutputStream
import java.io.OutputStream

class OutputStreamListener(out: OutputStream, private val consumer: (ByteArray) -> Unit) : FilterOutputStream(out) {
    override fun write(b: ByteArray?) {
        super.write(b)
        if (b != null) consumer(b)
    }
}
