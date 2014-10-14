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
		KeyPair keyPair = CryptoUtils.getInstance().generateKeyPair();
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
}
