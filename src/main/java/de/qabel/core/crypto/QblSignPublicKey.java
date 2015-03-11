package de.qabel.core.crypto;

import java.security.interfaces.RSAPublicKey;

/**
 * A QblSignPublic key can be only used to verify signatures
 */
public class QblSignPublicKey extends QblSubPublicKey {
	/**
	 *
	 */
	private static final long serialVersionUID = -8370695349101060329L;

	QblSignPublicKey(RSAPublicKey publicKey) {
		super(publicKey);
	}

	QblSignPublicKey(RSAPublicKey publicKey, byte[] primaryKeySignature) {
		super(publicKey, primaryKeySignature);
	}
}
