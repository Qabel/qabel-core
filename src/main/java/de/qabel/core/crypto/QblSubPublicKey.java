package de.qabel.core.crypto;

import java.security.interfaces.RSAPublicKey;

/**
 * Abstract super class for all QblSub...PublicKey types
 * 
 */
abstract class QblSubPublicKey extends QblPublicKey {

	private byte[] primaryKeySignature;

	QblSubPublicKey(RSAPublicKey publicKey) {
		super(publicKey);
	}

	QblSubPublicKey(RSAPublicKey publicKey, byte[] primaryKeySignature) {
		this(publicKey);
		setPrimaryKeySignature(primaryKeySignature);
	}

	/**
	 * Returns the signature for this sub-public key created by the primary
	 * private key
	 * 
	 * @return signature for this sub-public key
	 */
	byte[] getPrimaryKeySignature() {
		return primaryKeySignature;
	}

	/**
	 * Set the signature for this sub-public key. A QblPrimaryKeyPair has to
	 * call this method on all its sub keys after creation.
	 * 
	 * @param masterKeySignature
	 *            signature to attach to this sub-public key
	 */
	void setPrimaryKeySignature(byte[] masterKeySignature) {
		this.primaryKeySignature = masterKeySignature;
	}
}
