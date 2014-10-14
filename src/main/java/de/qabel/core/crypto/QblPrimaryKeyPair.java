package de.qabel.core.crypto;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

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

	QblPrimaryKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
		super();

		super.setRSAPrivateKey(privateKey);
		qblPrimaryPublicKey = new QblPrimaryPublicKey(publicKey);

		encKeyPairs = new ArrayList<QblEncKeyPair>();
		signKeyPairs = new ArrayList<QblSignKeyPair>();
	}

	QblPrimaryKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey,
			QblEncKeyPair encKeyPair, QblSignKeyPair signKeyPair) {
		this(privateKey, publicKey);
		attachEncKeyPair(encKeyPair);
		attachSignKeyPair(signKeyPair);
	}

	QblPrimaryKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey,
			List<QblEncKeyPair> encKeyPairs, List<QblSignKeyPair> signKeyPairs) {
		this(privateKey, publicKey);

		for (QblEncKeyPair qekp : encKeyPairs) {
			attachEncKeyPair(qekp);
		}

		for (QblSignKeyPair qskp : signKeyPairs) {
			attachSignKeyPair(qskp);
		}
	}

	/**
	 * Generates a new QblSignKeyPair
	 */
	void generateSignKeyPair() {
		QblSignKeyPair qskp = new QblSignKeyPair();
		attachSignKeyPair(qskp);
	}

	public void attachSignKeyPair(QblSignKeyPair qskp) {
		qskp.setQblPrimaryKeySignature(CryptoUtils.getInstance()
				.rsaSignKeyPair(qskp, this));
		signKeyPairs.add(qskp);
	}

	/**
	 * Generates a new QblEncKeyPair
	 */
	void generateEncKeyPair() {
		QblEncKeyPair qekp = new QblEncKeyPair();
		attachEncKeyPair(qekp);
	}

	public void attachEncKeyPair(QblEncKeyPair qekp) {
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
	public QblPrimaryPublicKey getQblPrimaryPublicKey() {
		return qblPrimaryPublicKey;
	}

	/**
	 * Returns an encryption public key
	 * 
	 * @return encryption public key
	 */
	public QblEncPublicKey getQblEncPublicKey() {
		// TODO: Implement support for multiple sub-keys
		return encKeyPairs.get(0).getQblEncPublicKey();
	}

	/**
	 * Returns a signature public key
	 * 
	 * @return signature public key
	 */
	public QblSignPublicKey getQblSignPublicKey() {
		// TODO: Implement support for multiple sub-keys
		return signKeyPairs.get(0).getQblSignPublicKey();
	}

	/**
	 * Returns an encryption private key
	 * 
	 * @return encryption private key
	 */
	public RSAPrivateKey getQblEncPrivateKey() {
		// TODO: Implement support for multiple sub-keys
		return encKeyPairs.get(0).getRSAPrivateKey();
	}

	/**
	 * Returns a signature private key
	 * 
	 * @return signature private key
	 */
	public RSAPrivateKey getQblSignPrivateKey() {
		// TODO: Implement support for multiple sub-keys
		return signKeyPairs.get(0).getRSAPrivateKey();
	}

	@Override
	public byte[] getPublicKeyFingerprint() {
		return qblPrimaryPublicKey.getPublicKeyFingerprint();
	}
}
