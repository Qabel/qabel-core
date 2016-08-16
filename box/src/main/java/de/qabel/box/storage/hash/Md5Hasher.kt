package de.qabel.box.storage.hash

import org.apache.commons.codec.digest.DigestUtils
import java.nio.file.Files
import java.nio.file.Path

class Md5Hasher : Hasher {
    override fun getMHash(file: Path): String {
        return String(DigestUtils.md5(Files.newInputStream(file)))
    }
}
