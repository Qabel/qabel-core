package de.qabel.core.crypto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QblKeyTest {

	@Test
	public void qblPrimaryKeyPairNotNullTest() {
		QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();

		assertNotNull(qpkp);
		assertNotNull(qpkp.getRSAPrivateKey());
		assertNotNull(qpkp.getQblPrimaryPublicKey());
		assertNotNull(qpkp.getQblEncPublicKey());
		assertNotNull(qpkp.getQblSignPublicKey());
		assertNotNull(qpkp.getPublicKeyFingerprint());
	}

	@Test
	public void qblPublicKeysNotNullTest() {
		QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();
		QblPrimaryPublicKey qppk = qpkp.getQblPrimaryPublicKey();
		QblEncPublicKey qepk = qpkp.getQblEncPublicKey();
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
	public void subKeySignatureValidTest() {
		QblPrimaryKeyPair qkp1 = new QblPrimaryKeyPair();

		// Validate signature of signature and encryption sub public keys
		assertTrue(CryptoUtils.getInstance().rsaValidateKeySignature(
				qkp1.getQblEncPublicKey(), qkp1.getQblPrimaryPublicKey()));

		assertTrue(CryptoUtils.getInstance().rsaValidateKeySignature(
				qkp1.getQblSignPublicKey(), qkp1.getQblPrimaryPublicKey()));

	}

	@Test
	public void subKeySignatureInvalidTest() {
		QblPrimaryKeyPair qkp1 = new QblPrimaryKeyPair();
		QblPrimaryKeyPair qkp2 = new QblPrimaryKeyPair();

		// Try to validate signature of signature and encryption sub public keys
		// with a wrong signature key
		assertFalse(CryptoUtils.getInstance().rsaValidateKeySignature(
				qkp1.getQblEncPublicKey(), qkp2.getQblPrimaryPublicKey()));

		assertFalse(CryptoUtils.getInstance().rsaValidateKeySignature(
				qkp1.getQblSignPublicKey(), qkp2.getQblPrimaryPublicKey()));

		assertFalse(CryptoUtils.getInstance().rsaValidateKeySignature(
				qkp2.getQblEncPublicKey(), qkp1.getQblPrimaryPublicKey()));

		assertFalse(CryptoUtils.getInstance().rsaValidateKeySignature(
				qkp2.getQblSignPublicKey(), qkp1.getQblPrimaryPublicKey()));
	}
}
