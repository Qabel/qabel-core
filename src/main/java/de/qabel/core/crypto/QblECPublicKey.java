package de.qabel.core.crypto;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Elliptic curve public key
 */
public class QblECPublicKey implements Serializable {
	public static final int KEY_SIZE_BYTE = 32;
	private byte[] pubKey;

	/**
	 * Generate elliptic curve public key from raw byte array
	 *
	 * @param pubKey
	 *            Point which represents the public key
	 */
	public QblECPublicKey(byte[] pubKey) {
		this.pubKey = pubKey;
	}

	/**
	 * Get encoded public key
	 *
	 * @return public key
	 */
	public byte[] getKey() {
		return pubKey;
	}

	public String getReadableKeyIdentifier() {
		return Hex.toHexString(pubKey);
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) { return true; }
		if(o == null || getClass() != o.getClass()) { return false; }

		QblECPublicKey that = (QblECPublicKey) o;

		if(!Arrays.equals(pubKey, that.pubKey)) { return false; }

		return true;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(pubKey);
	}
}
