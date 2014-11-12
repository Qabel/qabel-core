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

		KeyPair keyPair = QblKeyFactory.getInstance().generateKeyPair();
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
		qskp.setQblPrimaryKeySignature(QblKeyFactory.getInstance()
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
		qekp.setQblPrimaryKeySignature(QblKeyFactory.getInstance()
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((encKeyPairs == null) ? 0 : encKeyPairs.hashCode());
		result = prime
				* result
				+ ((qblPrimaryPublicKey == null) ? 0 : qblPrimaryPublicKey
						.hashCode());
		result = prime * result
				+ ((signKeyPairs == null) ? 0 : signKeyPairs.hashCode());
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
		QblPrimaryKeyPair other = (QblPrimaryKeyPair) obj;
		if (encKeyPairs == null) {
			if (other.encKeyPairs != null)
				return false;
		} else if (!encKeyPairs.equals(other.encKeyPairs))
			return false;
		if (qblPrimaryPublicKey == null) {
			if (other.qblPrimaryPublicKey != null)
				return false;
		} else if (!qblPrimaryPublicKey.equals(other.qblPrimaryPublicKey))
			return false;
		if (signKeyPairs == null) {
			if (other.signKeyPairs != null)
				return false;
		} else if (!signKeyPairs.equals(other.signKeyPairs))
			return false;
		return true;
	}
}
