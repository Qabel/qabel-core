package de.qabel.box.storage

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import de.qabel.core.util.Consumer

class ProgressInputStream(`in`: InputStream, private val consumer: Consumer<Long>) : FilterInputStream(`in`) {
    private var read: Long = 0

    @Throws(IOException::class)
    override fun read(): Int {
        val read = super.read()
        add(1)
        return read
    }

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
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read = super.read(b, off, len)
        add(read.toLong())
        return read
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val skip = super.skip(n)
        add(skip)
        return skip
    }

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
