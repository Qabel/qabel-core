package de.qabel.core.crypto;

import java.security.InvalidKeyException;

import org.meanbean.lang.Factory;

/**
 * QblPrimaryPublicKeyTestFactory
 * Creates distinct instances of class QblPrimaryPublicKey including sub keys
 * Attention: For testing purposes only
 */
public class QblPrimaryPublicKeyTestFactory implements Factory<QblPrimaryPublicKey> {
	@Override
	public QblPrimaryPublicKey create() {
		QblKeyFactory kf = QblKeyFactory.getInstance();
		QblPrimaryKeyPair qpkp = kf.generateQblPrimaryKeyPair();
		QblPrimaryPublicKey qppk = qpkp.getQblPrimaryPublicKey();
		try {
			qppk.attachEncPublicKey(qpkp.getQblEncPublicKeys().get(0));
			qppk.attachSignPublicKey(qpkp.getQblSignPublicKeys().get(0));
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return qppk;
	}
}
