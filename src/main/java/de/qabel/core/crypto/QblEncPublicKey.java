package de.qabel.core.crypto;

import java.security.interfaces.RSAPublicKey;

/**
 *  A QblEncPublicKey can be only used to encrypt a message for the owner
 *  of the related private key
 */
public class QblEncPublicKey extends QblSubPublicKey {
	
	QblEncPublicKey(RSAPublicKey publicKey) {
		super(publicKey);
	}
}
