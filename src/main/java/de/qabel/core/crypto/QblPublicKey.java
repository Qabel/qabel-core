package de.qabel.core.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

/**
 * Abstract super class for all Qbl...PublicKey types 
 *
 */
abstract class QblPublicKey {

	private RSAPublicKey publicKey;
	private byte[] publicKeyFingerprint;

	QblPublicKey(RSAPublicKey publicKey) {
		super();
		this.publicKey = publicKey;
		genFingerprint();
	}

	/**
	 * Returns the Javax RSAPublicKey
	 * @return Javax RSA public key
	 */
	RSAPublicKey getRSAPublicKey(){
		return publicKey;
	}
	
	/**
	 * Returns the modulus of the public key
	 * @return public key modulus
	 */
	BigInteger getModulus() {
		return publicKey.getModulus();
	}

	/**
	 * Returns the exponent of the public key
	 * @return public key exponent
	 */
	BigInteger getPublicExponent() {
		return publicKey.getPublicExponent();
	}

	/**
	 * Returns the public key fingerprint
	 * @return byte[ ] with public key fingerprint
	 */
	byte[] getPublicKeyFingerprint() {
		return publicKeyFingerprint;
	}

	/**
	 * Generates the public key fingerprint as a SHA512 digest
	 * of the public key modulus and exponent
	 */
	private void genFingerprint() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		try {
			bs.write((getPublicExponent().toByteArray()));
			bs.write((getModulus().toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		publicKeyFingerprint = CryptoUtils.getInstance().getSHA512sum(
				bs.toByteArray());
	}
}
