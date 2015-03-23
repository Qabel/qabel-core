package de.qabel.core.crypto;

import java.io.Serializable;
import java.security.interfaces.RSAPrivateKey;

/**
 * Abstract super class for all Qbl...KeyPair types 
 * 
 */
abstract class QblKeyPair implements Serializable {

	private RSAPrivateKey privateKey;

	QblKeyPair() {
		super();
	}

	void setRSAPrivateKey(RSAPrivateKey privateKey){
		this.privateKey = privateKey;
	}
	
	RSAPrivateKey getRSAPrivateKey() {
		return privateKey;
	}

	/**
	 * Returns the fingerprint of a public key. Created as a SHA512
	 * digest over the public key modulus and exponent.
	 * @return byte[ ] with the public key fingerprint
	 */
	abstract byte[] getPublicKeyFingerprint();
}
