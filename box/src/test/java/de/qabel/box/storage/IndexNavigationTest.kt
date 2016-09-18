package de.qabel.box.storage

import de.qabel.core.crypto.CryptoUtils
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class IndexNavigationTest : AbstractNavigationTest() {
    override val nav = DefaultIndexNavigation(dm, keyPair, volumeConfig)

    override fun encryptAndStream(cPath: File): InputStream? {
        val plaintext = IOUtils.toByteArray(FileInputStream(cPath))
        val encrypted = CryptoUtils().createBox(keyPair, keyPair.pub, plaintext, 0)
        return ByteArrayInputStream(encrypted)
    }
}
