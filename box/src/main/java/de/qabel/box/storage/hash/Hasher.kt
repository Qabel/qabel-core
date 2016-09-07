package de.qabel.box.storage.hash

import java.io.IOException
import java.nio.file.Path

interface Hasher {
    @Throws(IOException::class)
    fun getHash(file: Path): String
}
