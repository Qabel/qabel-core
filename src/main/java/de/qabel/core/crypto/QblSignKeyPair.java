package de.qabel.core.crypto;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * A QblSignKeyPair is a special key pair which can only be used
 * for signatures.
 */
public class QblSignKeyPair extends QblSubKeyPair {

	private QblSignPublicKey qblSignPublicKey;

	QblSignKeyPair() {
		super();
		KeyPair keyPair = QblKeyFactory.getInstance().generateKeyPair();
		super.setRSAPrivateKey((RSAPrivateKey) keyPair.getPrivate());
		qblSignPublicKey = new QblSignPublicKey(
				(RSAPublicKey) keyPair.getPublic());
	}
	
	QblSignKeyPair(RSAPrivateKey signPrivateKey, RSAPublicKey signPublicKey){
		super();
		super.setRSAPrivateKey(signPrivateKey);
		qblSignPublicKey = new QblSignPublicKey(signPublicKey);
	}
	
	/**
	 * Returns a public key which can be only used for signatures
	 * @return public key for signatures
	 */
	public QblSignPublicKey getQblSignPublicKey() {
		return qblSignPublicKey;
	}

	@Override
	void setQblPrimaryKeySignature(byte[] primaryKeySignature) {
		qblSignPublicKey.setPrimaryKeySignature(primaryKeySignature);
	}

	@Override
	public byte[] getPublicKeyFingerprint() {
		return qblSignPublicKey.getPublicKeyFingerprint();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((qblSignPublicKey == null) ? 0 : qblSignPublicKey.hashCode());
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
		QblSignKeyPair other = (QblSignKeyPair) obj;
		if (qblSignPublicKey == null) {
			if (other.qblSignPublicKey != null)
				return false;
		} else if (!qblSignPublicKey.equals(other.qblSignPublicKey))
			return false;
		return true;
	}
}
