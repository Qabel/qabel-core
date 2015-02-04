package de.qabel.core.crypto;

import java.security.InvalidKeyException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A QblPrimaryPublicKey represents a contact. This primary public key can be
 * used to identify valid encryption and signature sub-keys.
 */
public class QblPrimaryPublicKey extends QblPublicKey {

	private List<QblEncPublicKey> encPublicKeys;
	private List<QblSignPublicKey> signPublicKeys;
	
	private final static Logger logger = LogManager.getLogger(QblPrimaryPublicKey.class
			.getName());

	QblPrimaryPublicKey(RSAPublicKey publicKey) {
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
	 * @throws InvalidKeyException 
	 */
	public boolean attachEncPublicKey(QblEncPublicKey encPublicKey) throws InvalidKeyException {
		if (QblKeyFactory.getInstance().rsaValidateKeySignature(encPublicKey,
				this)) {
			encPublicKeys.add(encPublicKey);
			return true;
		}
		logger.debug("Didn't attach QblEncPublicKey due to invalid signature");
		return false;
	}

	/**
	 * Attach a new signature public key to the list of known public keys
	 * 
	 * @param signPublicKey
	 * @return key is valid and successfully attached to the list of known
	 *         public keys
	 * @throws InvalidKeyException 
	 */
	public boolean attachSignPublicKey(QblSignPublicKey signPublicKey) throws InvalidKeyException {
		if (QblKeyFactory.getInstance().rsaValidateKeySignature(signPublicKey,
				this)) {
			signPublicKeys.add(signPublicKey);
			return true;
		}
		logger.debug("Didn't attach QblSignPublicKey due to invalid signature");
		return false;
	}
	
	public List<QblEncPublicKey> getEncPublicKeys(){
		return encPublicKeys;
	}

	public List<QblSignPublicKey> getSignPublicKeys(){
		return signPublicKeys;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((encPublicKeys == null) ? 0 : encPublicKeys.hashCode());
		result = prime * result
				+ ((signPublicKeys == null) ? 0 : signPublicKeys.hashCode());
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
		QblPrimaryPublicKey other = (QblPrimaryPublicKey) obj;
		if (encPublicKeys == null) {
			if (other.encPublicKeys != null)
				return false;
		} else if (!encPublicKeys.equals(other.encPublicKeys))
			return false;
		if (signPublicKeys == null) {
			if (other.signPublicKeys != null)
				return false;
		} else if (!signPublicKeys.equals(other.signPublicKeys))
			return false;
		return true;
	}
}
