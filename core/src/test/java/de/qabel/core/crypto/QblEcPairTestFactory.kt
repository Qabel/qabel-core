package de.qabel.core.crypto

import de.qabel.core.crypto.QblECKeyPair
import org.meanbean.lang.Factory

/**
 * QblPrimaryKeyPairTestFactory
 * Creates distinct instances of class QblPrimaryKeyPair
 * Attention: For testing purposes only
 */
class QblEcPairTestFactory : Factory<QblECKeyPair> {
    lateinit internal var ecKeyPair: QblECKeyPair

    override fun create(): QblECKeyPair {
        ecKeyPair = QblECKeyPair()
        return ecKeyPair
    }
}
