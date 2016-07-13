package de.qabel.box.storage

import java.io.FilterInputStream
import java.io.InputStream

class InputStreamListener(input: InputStream, private val consumer: (ByteArray, Int) -> Unit) : FilterInputStream(input) {
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        return super.read(b, off, len).apply {
            if (b != null && this > 0) consumer(b, this)
        }
    }
}
