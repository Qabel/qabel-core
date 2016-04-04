package de.qabel.core.crypto;

import org.meanbean.lang.Factory;

/**
 * QblPrimaryKeyPairTestFactory
 * Creates distinct instances of class QblPrimaryKeyPair
 * Attention: For testing purposes only
 */
public class QblEcPairTestFactory implements Factory<QblECKeyPair> {
    QblECKeyPair ecKeyPair;

    public QblEcPairTestFactory() {
    }

    @Override
    public QblECKeyPair create() {
        ecKeyPair = new QblECKeyPair();
        return ecKeyPair;
    }
}
