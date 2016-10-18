package de.qabel.box.storage.hash

import java.io.File
import java.io.IOException

interface Hasher {
    @Throws(IOException::class)
    fun getHash(file: File): String
}
