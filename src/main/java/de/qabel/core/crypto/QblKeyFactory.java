package de.qabel.core.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * QblKeyFactory allows to generate new QblPrimaryKeyPairs and create QblPrivate
 * and QblPublic keys from the modulus and exponent
 *
 */
public class QblKeyFactory {

	private final static QblKeyFactory INSTANCE = new QblKeyFactory();
	
	private final static String CRYPTOGRAPHIC_PROVIDER = "BC"; // BouncyCastle
	private final static String KEY_ALGORITHM = "RSA";
	private final static int RSA_KEY_SIZE_BIT = 2048;
	private KeyFactory keyFactory;
	private KeyPairGenerator keyPairGen;
	private CryptoUtils cryptoUtils;

	private final static Logger logger = LogManager
			.getLogger(QblKeyFactory.class.getName());

	private QblKeyFactory() {
		try {
			Security.addProvider(new BouncyCastleProvider());
			
			cryptoUtils = new CryptoUtils();
			
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			keyPairGen.initialize(RSA_KEY_SIZE_BIT);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Cannot find selected algorithm! " + e.getMessage());
			throw new RuntimeException("Cannot find selected algorithm!", e);
		} catch (NoSuchProviderException e) {
			logger.error("Cannot find selected provider! " + e.getMessage());
			throw new RuntimeException("Cannot find selected provider!", e);
		}
	}

	public static QblKeyFactory getInstance() {
		return QblKeyFactory.INSTANCE;
	}

	/**
	 * Returns a new KeyPair
	 * 
	 * @return KeyPair
	 */
	KeyPair generateKeyPair() {
		return keyPairGen.generateKeyPair();
	}
	
	/**
	 * Signs a sub-key pair with a primary key
	 * 
	 * @param qkp
	 *            Sub-key pair to sign
	 * @param qpkp
	 *            Primary key pair to sign with
	 * @return byte[ ] with the signature. Can be null.
	 */
	synchronized byte[] rsaSignKeyPair(QblKeyPair qkp, QblPrimaryKeyPair qpkp) {

		if (qkp == null || qpkp == null) {
			return null;
		}
		return cryptoUtils.rsaSign(qkp.getPublicKeyFingerprint(), qpkp.getRSAPrivateKey());
	}

	/**
	 * Validates a signature from a sub-public key with a primary public key
	 * 
	 * @param subKey
	 *            Sub-public key to validate
	 * @param primaryKey
	 *            Primary public key to validate signature with
	 * @return is signature valid
	 * @throws InvalidKeyException
	 */
	synchronized boolean rsaValidateKeySignature(QblSubPublicKey subKey,
			QblPrimaryPublicKey primaryKey) throws InvalidKeyException {

		if (subKey == null || primaryKey == null) {
			return false;
		}
		return cryptoUtils.rsaValidateSignature(subKey.getPublicKeyFingerprint(),
				subKey.getPrimaryKeySignature(), primaryKey.getRSAPublicKey());
	}
	
	/**
	 * Generates the public key fingerprint as a SHA512 digest
	 * of the public key modulus and exponent
	 */
	synchronized byte[] getFingerprint(RSAPublicKey publicKey) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		try {
			bs.write((publicKey.getPublicExponent().toByteArray()));
			bs.write((publicKey.getModulus().toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cryptoUtils.getSHA512sum(bs.toByteArray());
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
	 * @param publicExponent
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
