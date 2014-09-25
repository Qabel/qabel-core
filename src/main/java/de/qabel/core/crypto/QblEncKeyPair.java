package de.qabel.core.crypto;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * A QblEncKeyPair is a special key pair which can only be used
 * for encryptions.
 */
public class QblEncKeyPair extends QblSubKeyPair {

	private QblEncPublicKey qblEncPublicKey;

	QblEncKeyPair() {
		super();
		KeyPair keyPair = CryptoUtils.getInstance().generateKeyPair();
		super.setRSAPrivateKey((RSAPrivateKey) keyPair.getPrivate());
		qblEncPublicKey = new QblEncPublicKey(
				(RSAPublicKey) keyPair.getPublic());
	}

	/**
	 * Returns the encryption public key
	 * @return encryption public key
	 */
	QblEncPublicKey getQblEncPublicKey() {
		return qblEncPublicKey;
	}

	@Override
	void setQblPrimaryKeySignature(byte[] primaryKeySignature) {
		qblEncPublicKey.setPrimaryKeySignature(primaryKeySignature);
	}

	@Override
	byte[] getPublicKeyFingerprint() {
		return qblEncPublicKey.getPublicKeyFingerprint();
	}
}
