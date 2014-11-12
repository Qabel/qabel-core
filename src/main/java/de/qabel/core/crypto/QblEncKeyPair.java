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
		KeyPair keyPair = QblKeyFactory.getInstance().generateKeyPair();
		super.setRSAPrivateKey((RSAPrivateKey) keyPair.getPrivate());
		qblEncPublicKey = new QblEncPublicKey(
				(RSAPublicKey) keyPair.getPublic());
	}
	
	QblEncKeyPair(RSAPrivateKey encPrivateKey, RSAPublicKey encPublicKey){
		super();
		super.setRSAPrivateKey(encPrivateKey);
		qblEncPublicKey = new QblEncPublicKey(encPublicKey);
	}

	/**
	 * Returns the encryption public key
	 * @return encryption public key
	 */
	public QblEncPublicKey getQblEncPublicKey() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((qblEncPublicKey == null) ? 0 : qblEncPublicKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QblEncKeyPair other = (QblEncKeyPair) obj;
		if (qblEncPublicKey == null) {
			if (other.qblEncPublicKey != null)
				return false;
		} else if (!qblEncPublicKey.equals(other.qblEncPublicKey))
			return false;
		return true;
	}
}
