package de.qabel.core.crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * QblKeyFactory allows to generate new QblPrimaryKeyPairs and create QblPrivate
 * and QblPublic keys from the modulus and exponent
 *
 */
public class QblKeyFactory {

	private final static QblKeyFactory INSTANCE = new QblKeyFactory();
	private final static String KEY_ALGORITHM = "RSA";
	private KeyFactory keyFactory;

	private final static Logger logger = LogManager
			.getLogger(QblKeyFactory.class.getName());

	private QblKeyFactory() {
		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Cannot find selected algorithm! " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static QblKeyFactory getInstance() {
		return QblKeyFactory.INSTANCE;
	}

	/**
	 * Creates a new RSAPrivate key
	 * 
	 * @param modulus
	 *            RSA modulus
	 * @param privateExponent
	 *            RSA private exponent
	 * @return new RSAPrivateKey
	 * @throws InvalidKeySpecException
	 *             if modulus or exponent are invalid for RSA keys
	 */
	private RSAPrivateKey generateRSAPrivateKey(BigInteger modulus,
			BigInteger privateExponent) throws InvalidKeySpecException {
		return (RSAPrivateKey) keyFactory
				.generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
	}

	/**
	 * Creates a new RSAPublic key
	 * 
	 * @param modulus
	 *            RSA modulus
	 * @param privateExponent
	 *            RSA public exponent
	 * @return new RSAPublicKey
	 * @throws InvalidKeySpecException
	 *             if modulus or exponent are invalid for RSA keys
	 */
	private RSAPublicKey generateRSAPublicKey(BigInteger modulus,
			BigInteger publicExponent) throws InvalidKeySpecException {
		return (RSAPublicKey) keyFactory.generatePublic(new RSAPublicKeySpec(
				modulus, publicExponent));
	}

	/**
	 * Generates a new QblPrimaryKeyPair with a QblEncKeyPair and
	 * QblSignKeyPair attached
	 * 
	 * @return newly generated QblPrimaryKeyPair
	 */
	public QblPrimaryKeyPair generateQblPrimaryKeyPair() {
		return new QblPrimaryKeyPair();
	}

	/**
	 * (Re)-creates a QblPrimaryKeyPair from the modulus, public and private
	 * exponents.
	 * 
	 * @param modulus
	 *            shared RSA modulus
	 * @param privateExponent
	 *            private RSA exponent
	 * @param publicExponent
	 *            public RSA exponent
	 * @return created QblPrimaryKeyPair
	 * @throws InvalidKeySpecException
	 *             if modulus or exponents are invalid for RSA keys
	 */
	public QblPrimaryKeyPair createQblPrimaryKeyPair(BigInteger modulus,
			BigInteger privateExponent, BigInteger publicExponent)
			throws InvalidKeySpecException {
		RSAPrivateKey privateKey = generateRSAPrivateKey(modulus,
				privateExponent);
		RSAPublicKey publicKey = generateRSAPublicKey(modulus, publicExponent);
		return new QblPrimaryKeyPair(privateKey, publicKey);
	}

	/**
	 * (Re)-creates a QblEncKeyPair from the modulus, public and private
	 * exponents.
	 * 
	 * @param modulus
	 *            shared RSA modulus
	 * @param privateExponent
	 *            private RSA exponent
	 * @param publicExponent
	 *            public RSA exponent
	 * @return created QblEncKeyPair
	 * @throws InvalidKeySpecException
	 *             if modulus or exponents are invalid for RSA keys
	 */
	public QblEncKeyPair createQblEncKeyPair(BigInteger modulus,
			BigInteger privateExponent, BigInteger publicExponent)
			throws InvalidKeySpecException {
		RSAPrivateKey privateKey = generateRSAPrivateKey(modulus,
				privateExponent);
		RSAPublicKey publicKey = generateRSAPublicKey(modulus, publicExponent);
		return new QblEncKeyPair(privateKey, publicKey);
	}

	/**
	 * (Re)-creates a QblSignKeyPair from the modulus, public and private
	 * exponents.
	 * 
	 * @param modulus
	 *            shared RSA modulus
	 * @param privateExponent
	 *            private RSA exponent
	 * @param publicExponent
	 *            public RSA exponent
	 * @return created QblSignKeyPair
	 * @throws InvalidKeySpecException
	 *             if modulus or exponents are invalid for RSA keys
	 */
	public QblSignKeyPair createQblSignKeyPair(BigInteger modulus,
			BigInteger privateExponent, BigInteger publicExponent)
			throws InvalidKeySpecException {
		RSAPrivateKey privateKey = generateRSAPrivateKey(modulus,
				privateExponent);
		RSAPublicKey publicKey = generateRSAPublicKey(modulus, publicExponent);
		return new QblSignKeyPair(privateKey, publicKey);
	}

	/**
	 * (Re)-creates a QblPrimaryPublicKey from the modulus and public exponent.
	 * 
	 * @param modulus
	 *            RSA modulus
	 * @param publicExponent
	 *            public RSA exponent
	 * @return created QblPrimaryPublicKey
	 * @throws InvalidKeySpecException
	 *             if modulus or exponent are invalid for RSA keys
	 */
	public QblPrimaryPublicKey createQblPrimaryPublicKey(BigInteger modulus,
			BigInteger publicExponent) throws InvalidKeySpecException {
		RSAPublicKey publicKey = generateRSAPublicKey(modulus, publicExponent);
		return new QblPrimaryPublicKey(publicKey);
	}

	/**
	 * (Re)-creates a QblEncPublicKey from the modulus and public exponent.
	 * 
	 * @param modulus
	 *            RSA modulus
	 * @param publicExponent
	 *            public RSA exponent
	 * @return created QblEncPublicKey
	 * @throws InvalidKeySpecException
	 *             if modulus or exponent are invalid for RSA keys
	 */
	public QblEncPublicKey createQblEncPublicKey(BigInteger modulus,
			BigInteger publicExponent, byte[] primaryKeySignature)
			throws InvalidKeySpecException, IllegalArgumentException {
		if (primaryKeySignature == null) {
			throw new IllegalArgumentException(
					"primaryKeySignature must be set");
		}
		RSAPublicKey publicKey = generateRSAPublicKey(modulus, publicExponent);
		return new QblEncPublicKey(publicKey, primaryKeySignature);
	}

	/**
	 * (Re)-creates a QblSignPublicKey from the modulus and public exponent.
	 * 
	 * @param modulus
	 *            RSA modulus
	 * @param publicExponent
	 *            public RSA exponent
	 * @return created QblSignPublicKey
	 * @throws InvalidKeySpecException
	 *             if modulus or exponent are invalid for RSA keys
	 */
	public QblSignPublicKey createQblSignPublicKey(BigInteger modulus,
			BigInteger publicExponent, byte[] primaryKeySignature)
			throws InvalidKeySpecException, IllegalArgumentException {
		if (primaryKeySignature == null) {
			throw new IllegalArgumentException(
					"primaryKeySignature must be set");
		}
		RSAPublicKey publicKey = generateRSAPublicKey(modulus, publicExponent);
		return new QblSignPublicKey(publicKey, primaryKeySignature);
	}
}
