package de.qabel.core.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;

import org.junit.Test;

public class QblKeyTest {

	@Test
	public void qblPrimaryKeyPairNotNullTest() {
		QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();

		assertNotNull(qpkp);
		assertNotNull(qpkp.getRSAPrivateKey());
		assertNotNull(qpkp.getQblPrimaryPublicKey());
		assertNotNull(qpkp.getQblEncPublicKeys().get(0));
		assertNotNull(qpkp.getQblSignPublicKey());
		assertNotNull(qpkp.getPublicKeyFingerprint());
	}

	@Test
	public void qblPublicKeysNotNullTest() {
		QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();
		QblPrimaryPublicKey qppk = qpkp.getQblPrimaryPublicKey();
		QblEncPublicKey qepk = qpkp.getQblEncPublicKeys().get(0);
		QblSignPublicKey qspk = qpkp.getQblSignPublicKey();

		assertNotNull(qpkp);
		assertNotNull(qppk);
		assertNotNull(qepk);
		assertNotNull(qspk);

		assertNotNull(qppk.getModulus());
		assertNotNull(qppk.getPublicExponent());
		assertNotNull(qppk.getPublicKeyFingerprint());
		assertNotNull(qppk.getRSAPublicKey());

		assertNotNull(qepk.getPublicKeyFingerprint());
		assertNotNull(qepk.getModulus());
		assertNotNull(qepk.getPublicExponent());
		assertNotNull(qepk.getPublicKeyFingerprint());
		assertNotNull(qepk.getRSAPublicKey());

		assertNotNull(qspk.getPublicKeyFingerprint());
		assertNotNull(qspk.getModulus());
		assertNotNull(qspk.getPublicExponent());
		assertNotNull(qspk.getPublicKeyFingerprint());
		assertNotNull(qspk.getRSAPublicKey());
	}

	@Test
	public void subKeySignatureValidTest() throws InvalidKeyException {
		QblPrimaryKeyPair qkp1 = new QblPrimaryKeyPair();

		// Validate signature of signature and encryption sub public keys
		assertTrue(QblKeyFactory.getInstance().rsaValidateKeySignature(
				qkp1.getQblEncPublicKeys().get(0), qkp1.getQblPrimaryPublicKey()));

		assertTrue(QblKeyFactory.getInstance().rsaValidateKeySignature(
				qkp1.getQblSignPublicKey(), qkp1.getQblPrimaryPublicKey()));

	}

	@Test
	public void subKeySignatureInvalidTest() throws InvalidKeyException {
		QblPrimaryKeyPair qkp1 = new QblPrimaryKeyPair();
		QblPrimaryKeyPair qkp2 = new QblPrimaryKeyPair();

		// Try to validate signature of signature and encryption sub public keys
		// with a wrong signature key
		assertFalse(QblKeyFactory.getInstance().rsaValidateKeySignature(
				qkp1.getQblEncPublicKeys().get(0), qkp2.getQblPrimaryPublicKey()));

		assertFalse(QblKeyFactory.getInstance().rsaValidateKeySignature(
				qkp1.getQblSignPublicKey(), qkp2.getQblPrimaryPublicKey()));

		assertFalse(QblKeyFactory.getInstance().rsaValidateKeySignature(
				qkp2.getQblEncPublicKeys().get(0), qkp1.getQblPrimaryPublicKey()));

		assertFalse(QblKeyFactory.getInstance().rsaValidateKeySignature(
				qkp2.getQblSignPublicKey(), qkp1.getQblPrimaryPublicKey()));
	}
}
