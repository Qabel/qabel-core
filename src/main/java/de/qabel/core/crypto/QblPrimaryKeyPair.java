package de.qabel.core.crypto;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

/**
 * A QblPrimaryKeyPair represents the own identity. Is contains at least one
 * encryption and one signature sub-keypair.
 *
 */
public class QblPrimaryKeyPair extends QblKeyPair {

	private QblPrimaryPublicKey qblPrimaryPublicKey;
	private ArrayList<QblEncKeyPair> encKeyPairs;
	private ArrayList<QblSignKeyPair> signKeyPairs;

	QblPrimaryKeyPair() {
		super();

		KeyPair keyPair = CryptoUtils.getInstance().generateKeyPair();
		super.setRSAPrivateKey((RSAPrivateKey) keyPair.getPrivate());
		qblPrimaryPublicKey = new QblPrimaryPublicKey(
				(RSAPublicKey) keyPair.getPublic());

		encKeyPairs = new ArrayList<QblEncKeyPair>();
		signKeyPairs = new ArrayList<QblSignKeyPair>();

		generateEncKeyPair();
		generateSignKeyPair();
	}

	/**
	 * Generates a new QblSignKeyPair
	 */
	private void generateSignKeyPair() {
		QblSignKeyPair qskp = new QblSignKeyPair();
		qskp.setQblPrimaryKeySignature(CryptoUtils.getInstance()
				.rsaSignKeyPair(qskp, this));
		signKeyPairs.add(qskp);
	}

	/**
	 * Generates a new QblEncKeyPair
	 */
	private void generateEncKeyPair() {
		QblEncKeyPair qekp = new QblEncKeyPair();
		qekp.setQblPrimaryKeySignature(CryptoUtils.getInstance()
				.rsaSignKeyPair(qekp, this));
		encKeyPairs.add(qekp);
	}

	public QblEncKeyPair getEncKeyPairs() {
		return encKeyPairs.get(0);
	}

	public QblSignKeyPair getSignKeyPairs() {
		return signKeyPairs.get(0);
	}

	/**
	 * Returns the primary public key
	 * 
	 * @return primary public key
	 */
	QblPrimaryPublicKey getQblPrimaryPublicKey() {
		return qblPrimaryPublicKey;
	}

	/**
	 * Returns an encryption public key
	 * 
	 * @return encryption public key
	 */
	QblEncPublicKey getQblEncPublicKey() {
		// TODO: Implement support for multiple sub-keys
		return encKeyPairs.get(0).getQblEncPublicKey();
	}

	/**
	 * Returns a signature public key
	 * 
	 * @return signature public key
	 */
	QblSignPublicKey getQblSignPublicKey() {
		// TODO: Implement support for multiple sub-keys
		return signKeyPairs.get(0).getQblSignPublicKey();
	}

	/**
	 * Returns an encryption private key
	 * 
	 * @return encryption private key
	 */
	RSAPrivateKey getQblEncPrivateKey() {
		// TODO: Implement support for multiple sub-keys
		return encKeyPairs.get(0).getRSAPrivateKey();
	}

	/**
	 * Returns a signature private key
	 * 
	 * @return signature private key
	 */
	RSAPrivateKey getQblSignPrivateKey() {
		// TODO: Implement support for multiple sub-keys
		return signKeyPairs.get(0).getRSAPrivateKey();
	}

	@Override
	public byte[] getPublicKeyFingerprint() {
		return qblPrimaryPublicKey.getPublicKeyFingerprint();
	}
}
