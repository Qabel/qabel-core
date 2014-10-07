package de.qabel.core.crypto;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

/**
 * A QblPrimaryPublicKey represents a contact. This primary public key can be
 * used to identify valid encryption and signature sub-keys.
 */
public class QblPrimaryPublicKey extends QblPublicKey {

	private ArrayList<QblEncPublicKey> encPublicKeys;
	private ArrayList<QblSignPublicKey> signPublicKeys;

	public QblPrimaryPublicKey(RSAPublicKey publicKey) {
		super(publicKey);
		encPublicKeys = new ArrayList<QblEncPublicKey>();
		signPublicKeys = new ArrayList<QblSignPublicKey>();
	}

	/**
	 * Attach a new encryption public key to the list of known public keys
	 * 
	 * @param encPublicKey
	 * @return key is valid and successfully attached to the list of known
	 *         public keys
	 */
	boolean attachEncPublicKey(QblEncPublicKey encPublicKey) {
		if (CryptoUtils.getInstance().rsaValidateKeySignature(encPublicKey,
				this)) {
			encPublicKeys.add(encPublicKey);
			return true;
		}
		return false;
	}

	/**
	 * Attach a new signature public key to the list of known public keys
	 * 
	 * @param signPublicKey
	 * @return key is valid and successfully attached to the list of known
	 *         public keys
	 */
	boolean attachSignPublicKey(QblSignPublicKey signPublicKey) {
		if (CryptoUtils.getInstance().rsaValidateKeySignature(signPublicKey,
				this)) {
			signPublicKeys.add(signPublicKey);
			return true;
		}
		return false;
	}
	
	public QblEncPublicKey getEncPublicKey(){
		// TODO: Implement support for multiple sub-keys
		return encPublicKeys.get(0);
	}
	
	public QblSignPublicKey getSignPublicKey(){
		// TODO: Implement support for multiple sub-keys
		return signPublicKeys.get(0);
	}
}
