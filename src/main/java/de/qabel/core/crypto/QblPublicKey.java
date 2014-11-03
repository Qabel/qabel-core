package de.qabel.core.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

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
	public RSAPublicKey getRSAPublicKey(){
		return publicKey;
	}
	
	/**
	 * Returns the modulus of the public key
	 * @return public key modulus
	 */
	public BigInteger getModulus() {
		return publicKey.getModulus();
	}

	/**
	 * Returns the exponent of the public key
	 * @return public key exponent
	 */
	public BigInteger getPublicExponent() {
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
	* Get the key identifier of this public key
	*
	* @return key identifier
	*/	
	public byte[] getKeyIdentifier() {
		byte[] publicFingerPrint = getPublicKeyFingerprint();
		return Arrays.copyOfRange(publicFingerPrint,
				publicFingerPrint.length - 8, publicFingerPrint.length);
	}

	/**
	 * Get the key identifier of this public key in a human readable HEX string
	 *
	 * @return key identifier as readable HEX string
	 */
	public String getReadableKeyIdentifier() {
		return DatatypeConverter.printHexBinary(getKeyIdentifier());
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((publicKey == null) ? 0 : publicKey.hashCode());
		result = prime * result + Arrays.hashCode(publicKeyFingerprint);
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
		QblPublicKey other = (QblPublicKey) obj;
		if (publicKey == null) {
			if (other.publicKey != null)
				return false;
		} else if (!publicKey.equals(other.publicKey))
			return false;
		if (!Arrays.equals(publicKeyFingerprint, other.publicKeyFingerprint))
			return false;
		return true;
	}
}
