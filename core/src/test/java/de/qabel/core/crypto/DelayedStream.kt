package de.qabel.core.crypto

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class DelayedStream(`in`: InputStream) : FilterInputStream(`in`) {
    private var blockOnRead = true
    var isBlocked: Boolean = false
        private set

    @Throws(IOException::class)
    override fun read(): Int {
        while (isBlocked) {
            Thread.`yield`()
        }
        if (blockOnRead) {
            block()
        }
        return super.read()
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        while (isBlocked) {
            Thread.`yield`()
        }
        if (blockOnRead) {
            block()
        }
        return super.read(b, off, len)
    }

    fun block() {
        isBlocked = true
    }

    fun unblock() {
        isBlocked = false
    }

    fun setBlockOnRead(blockOnRead: Boolean) {
        this.blockOnRead = blockOnRead
    }
}
