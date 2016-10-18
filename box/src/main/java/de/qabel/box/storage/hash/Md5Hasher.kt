package de.qabel.box.storage.hash

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream

class Md5Hasher : Hasher {
    override fun getHash(file: File) = FileInputStream(file).use {
        DigestUtils.md5Hex(it)
    }
}
