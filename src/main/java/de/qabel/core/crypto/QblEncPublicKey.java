package de.qabel.core.crypto;

import java.security.interfaces.RSAPublicKey;

/**
 * A QblEncPublicKey can be only used to encrypt a message for the owner of the
 * related private key
 */
public class QblEncPublicKey extends QblSubPublicKey {
	/**
	 *
	 */
	private static final long serialVersionUID = 259177911416775073L;

	QblEncPublicKey(RSAPublicKey publicKey) {
		super(publicKey);
	}

	QblEncPublicKey(RSAPublicKey publicKey, byte[] primaryKeySignature) {
		super(publicKey, primaryKeySignature);
	}
}
