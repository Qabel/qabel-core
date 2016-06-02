package de.qabel.core.crypto;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import org.meanbean.lang.Factory;

/**
 * QblPrimaryPublicKeyTestFactory
 * Creates distinct instances of class QblPrimaryPublicKey including sub keys
 * Attention: For testing purposes only
 */
public class QblECPublicKeyTestFactory implements Factory<QblECPublicKey> {
    @Override
    public QblECPublicKey create() {
        QblECKeyPair ecKeyPair = new QblECKeyPair();
        return ecKeyPair.getPub();
    }
}
