package de.qabel.box.storage

import java.io.Closeable
import java.io.IOException
import java.io.InputStream

class StorageDownload @JvmOverloads constructor(val inputStream: InputStream, val mHash: String, val size: Long, private val closeable: Closeable? = null) : Closeable {

    @Throws(IOException::class)
    override fun close() {
        closeable?.close()
    }
}
