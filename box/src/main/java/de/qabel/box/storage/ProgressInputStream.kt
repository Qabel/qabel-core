package de.qabel.box.storage

import de.qabel.core.util.Consumer
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors

class ProgressInputStream(`in`: InputStream, private val consumer: Consumer<Long>) : FilterInputStream(`in`) {
    private var read: Long = 0

    @Throws(IOException::class)
    override fun read() = super.read().apply {add(1)}

    private fun add(bytes: Long) {
        if (bytes <= 0) {
            return
        }
        read += bytes
        update()
    }

    private fun update() {
        executor.submit { consumer.accept(read) }
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int) = super.read(b, off, len).apply {add(toLong())}

    @Throws(IOException::class)
    override fun skip(n: Long) = super.skip(n).apply {add(this)}

    @Throws(IOException::class)
    override fun close() {
        update()
        super.close()
    }

    @Synchronized @Throws(IOException::class)
    override fun reset() {
        super.reset()
    }

    companion object {
        private val executor = Executors.newSingleThreadExecutor()
    }
}
