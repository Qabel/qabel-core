package de.qabel.core.crypto;

import org.meanbean.lang.Factory;

/**
 * QblPrimaryKeyPairTestFactory
 * Creates distinct instances of class QblPrimaryKeyPair
 * Attention: For testing purposes only
 */
public class QblPrimaryKeyPairTestFactory implements Factory<QblPrimaryKeyPair> {
	QblKeyFactory kf;
	QblPrimaryKeyPair qpkp;

	public QblPrimaryKeyPairTestFactory () {
		kf = QblKeyFactory.getInstance();
	}

	@Override
	public QblPrimaryKeyPair create() {
		qpkp = kf.generateQblPrimaryKeyPair();
		return qpkp;
	}
}
