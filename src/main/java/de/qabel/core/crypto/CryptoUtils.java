package de.qabel.core.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.*;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtils {

	private final static CryptoUtils INSTANCE = new CryptoUtils();

	private final static String CRYPTOGRAPHIC_PROVIDER = "BC"; // BouncyCastle
	// https://github.com/Qabel/qabel-doc/wiki/Components-Crypto
	private final static String ASYM_KEY_ALGORITHM = "RSA";
	private final static String MESSAGE_DIGEST_ALGORITHM = "SHA-512";
	private final static String SIGNATURE_ALGORITHM = "RSASSA-PSS";
	private final static String RSA_CIPHER_ALGORITM = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";
	private final static String HMAC_ALGORITHM = "HMac/" + "SHA512";
	private final static int RSA_SIGNATURE_SIZE_BYTE = 256;
	private final static int RSA_KEY_SIZE_BIT = 2048;
	private final static String SYMM_KEY_ALGORITHM = "AES";
	private final static String SYMM_TRANSFORMATION = "AES/CTR/NoPadding";
	private final static String SYMM_ALT_TRANSFORMATION = "AES/GCM/NoPadding";
	private final static int SYMM_IV_SIZE_BIT = 128;
	private final static int SYMM_NONCE_SIZE_BIT = 96;
	private final static int AES_KEY_SIZE_BYTE = 32;
	private final static int ENCRYPTED_AES_KEY_SIZE_BYTE = 256;

	private final static Logger logger = LogManager.getLogger(CryptoUtils.class
			.getName());

	private KeyPairGenerator keyGen;
	private SecureRandom secRandom;
	private MessageDigest messageDigest;
	private Cipher symmetricCipher;
	private Cipher asymmetricCipher;
	private Cipher gcmCipher;
	private Signature signer;
	private Mac hmac;

	private CryptoUtils() {

		try {
			Security.addProvider(new BouncyCastleProvider());

			secRandom = new SecureRandom();

			keyGen = KeyPairGenerator.getInstance(ASYM_KEY_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			keyGen.initialize(RSA_KEY_SIZE_BIT);

			messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			symmetricCipher = Cipher.getInstance(SYMM_TRANSFORMATION,
					CRYPTOGRAPHIC_PROVIDER);
			asymmetricCipher = Cipher.getInstance(RSA_CIPHER_ALGORITM,
					CRYPTOGRAPHIC_PROVIDER);
			gcmCipher = Cipher.getInstance(SYMM_ALT_TRANSFORMATION,
					CRYPTOGRAPHIC_PROVIDER);
			signer = Signature.getInstance(SIGNATURE_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			hmac = Mac.getInstance(HMAC_ALGORITHM, CRYPTOGRAPHIC_PROVIDER);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Cannot find selected algorithm! " + e.getMessage());
			throw new RuntimeException("Cannot find selected algorithm!", e);
		} catch (NoSuchPaddingException e) {
			logger.error("Cannot find selected padding! " + e.getMessage());
			throw new RuntimeException("Cannot find selected padding!", e);
		} catch (NoSuchProviderException e) {
			logger.error("Cannot find selected provider! " + e.getMessage());
			throw new RuntimeException("Cannot find selected provider!", e);
		}
	}

	public static CryptoUtils getInstance() {
		return CryptoUtils.INSTANCE;
	}

	/**
	 * Returns a new KeyPair
	 * 
	 * @return KeyPair
	 */
	synchronized KeyPair generateKeyPair() {
		return keyGen.generateKeyPair();
	}

	/**
	 * Returns a random byte array with an arbitrary size
	 * 
	 * @param numBytes
	 *            Number of random bytes
	 * @return byte[ ] with random bytes
	 */
	synchronized byte[] getRandomBytes(int numBytes) {
		byte[] ranBytes = new byte[numBytes];
		secRandom.nextBytes(ranBytes);
		return ranBytes;
	}

	/**
	 * Returns the SHA512 digest for a byte array
	 * 
	 * @param bytes
	 *            byte[ ] to get the digest from
	 * @return byte[ ] with SHA512 digest
	 */
	public synchronized byte[] getSHA512sum(byte[] bytes) {
		byte[] digest = messageDigest.digest(bytes);
		return digest;
	}

	/**
	 * Returns the SHA512 digest for a byte array
	 * 
	 * @param bytes
	 *            byte[ ] to get the digest from
	 * @return SHA512 digest as as String in the following format:
	 *         "00:11:aa:bb:..."
	 */
	public synchronized String getSHA512sumHumanReadable(byte[] bytes) {
		byte[] digest = getSHA512sum(bytes);

		StringBuilder sb = new StringBuilder(191);

		for (int i = 0; i < digest.length - 1; i++) {
			sb.append(String.format("%02x", digest[i] & 0xff));
			sb.append(":");
		}
		sb.append(String.format("%02x", digest[digest.length - 1] & 0xff));
		return sb.toString();
	}

	/**
	 * Returns the SHA512 digest for a String
	 * 
	 * @param plain
	 *            Input String
	 * @return byte[ ] with SHA512 digest
	 */
	public synchronized byte[] getSHA512sum(String plain) {
		return getSHA512sum(plain.getBytes());
	}

	/**
	 * Returns the SHA512 digest for a String
	 * 
	 * @param plain
	 *            Input String
	 * @return SHA512 digest as as String in the following format:
	 *         "00:11:aa:bb:..."
	 */
	public synchronized String getSHA512sumHumanReadable(String plain) {
		return getSHA512sumHumanReadable(plain.getBytes());
	}

	/**
	 * Create a signature over the SHA512 sum of message with signature key
	 * 
	 * @param message
	 *            Message to create signature for
	 * @param signatureKey
	 *            Signature key to sign with
	 * @return Signature over SHA512 sum of message
	 */
	private byte[] createSignature(byte[] message, QblSignKeyPair signatureKey) {
		byte[] sha512Sum = getSHA512sum(message);
		return rsaSign(sha512Sum, signatureKey);
	}

	/**
	 * Sign data with RSA
	 * 
	 * @param data
	 *            Data to sign. Usually a message digest.
	 * @param qpkp
	 *            QblPrimaryKeyPair to extract signature key from
	 * @return Signature of data
	 */
	private byte[] rsaSign(byte[] data, QblPrimaryKeyPair qpkp) {
		return rsaSign(data, qpkp.getQblSignPrivateKey());
	}

	/**
	 * Sign data with RSA
	 * 
	 * @param data
	 *            Data to sign. Usually a message digest.
	 * @param signatureKey
	 *            QblSignKeyPair to extract signature key from
	 * @return Signature of data
	 */
	private byte[] rsaSign(byte[] data, QblSignKeyPair signatureKey) {
		return rsaSign(data, signatureKey.getRSAPrivateKey());
	}

	/**
	 * Sign data with RSA
	 * 
	 * @param data
	 *            Data to sign. Usually a message digest.
	 * @param signatureKey
	 *            QblSignKeyPair to extract signature key from
	 * @return Signature of data. Can be null if error occured.
	 */
	private byte[] rsaSign(byte[] data, RSAPrivateKey signatureKey) {
		byte[] sign = null;
		try {
			signer.initSign(signatureKey);
			signer.update(data);
			sign = signer.sign();
		} catch (InvalidKeyException e) {
			logger.error("Invalid key!");
		} catch (SignatureException e) {
			logger.error("Signature exception!");
		}
		return sign;
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
		return rsaSign(qkp.getPublicKeyFingerprint(), qpkp.getRSAPrivateKey());
	}

	/**
	 * Validates the signature of a message. The SHA512 digest of the message is
	 * validated against the provided signature.
	 * 
	 * @param message
	 *            Message to validate signature from
	 * @param signature
	 *            Signature to validate
	 * @param signPublicKey
	 *            Public key to validate signature with
	 * @return is signature valid
	 * @throws InvalidKeyException
	 */
	private boolean validateSignature(byte[] message, byte[] signature,
			QblSignPublicKey signPublicKey) throws InvalidKeyException {
		byte[] sha512Sum = getSHA512sum(message);
		return rsaValidateSignature(sha512Sum, signature,
				signPublicKey.getRSAPublicKey());
	}

	/**
	 * Validate the RSA signature of a data.
	 * 
	 * @param data
	 *            Data to validate signature from. Usually a message digest.
	 * @param signature
	 *            Signature to validate with
	 * @param signatureKey
	 *            Public key to validate signature with
	 * @return is signature valid
	 * @throws InvalidKeyException
	 */
	private boolean rsaValidateSignature(byte[] data, byte[] signature,
			RSAPublicKey signatureKey) throws InvalidKeyException {
		boolean isValid = false;
		try {
			signer.initVerify(signatureKey);
			signer.update(data);
			isValid = signer.verify(signature);
		} catch (InvalidKeyException e) {
			logger.error("Invalid RSA public key!");
			throw new InvalidKeyException("Invalid RSA public key!");
		} catch (SignatureException e) {
			logger.error("Signature exception!");
		}
		return isValid;
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
		return rsaValidateSignature(subKey.getPublicKeyFingerprint(),
				subKey.getPrimaryKeySignature(), primaryKey.getRSAPublicKey());
	}

	/**
	 * Encrypts a byte[ ] with RSA
	 * 
	 * @param message
	 *            message to encrypt
	 * @param reciPubKey
	 *            public key to encrypt with
	 * @return encrypted messsage. Can be null if error occurred.
	 * @throws InvalidKeyException
	 */
	private byte[] rsaEncryptForRecipient(byte[] message,
			QblEncPublicKey reciPubKey) throws InvalidKeyException {
		byte[] cipherText = null;
		try {
			asymmetricCipher.init(Cipher.ENCRYPT_MODE,
					reciPubKey.getRSAPublicKey(), secRandom);
			cipherText = asymmetricCipher.doFinal(message);
		} catch (InvalidKeyException e) {
			logger.error("Invalid RSA public key!");
			throw new InvalidKeyException("Invalid RSA public key!");
		} catch (IllegalBlockSizeException e) {
			logger.error("Illegal block size!");
		} catch (BadPaddingException e) {
			logger.error("Bad padding!");
		}
		return cipherText;
	}

	/**
	 * Decrypts a RSA encrypted ciphertext
	 * 
	 * @param cipherText
	 *            ciphertext to decrypt
	 * @param privKey
	 *            private key to decrypt with
	 * @return decrypted ciphertext, or null if undecryptable
	 * @throws InvalidKeyException
	 */
	private byte[] rsaDecrypt(byte[] cipherText, RSAPrivateKey privKey)
			throws InvalidKeyException {
		byte[] plaintext = null;
		try {
			asymmetricCipher.init(Cipher.DECRYPT_MODE, privKey, secRandom);
			plaintext = asymmetricCipher.doFinal(cipherText);
		} catch (InvalidKeyException e) {
			logger.error("Invalid RSA private key!");
			throw new InvalidKeyException("Invalid RSA private key!");
		} catch (IllegalBlockSizeException e) {
			logger.error("Illegal block size!");
		} catch (BadPaddingException e) {
			// This exception should occur if cipherText is decrypted with wrong
			// private key
			return null;
		} catch (DataLengthException e) {
			// This exception only occurs with Bouncy Castle while decrypting
			// with a wrong private key
			return null;
		}

		return plaintext;
	}

	/**
	 * Returns the encrypted byte[] of the given plaintext, i.e.
	 * ciphertext=enc(plaintext,key) The algorithm, mode and padding is set in
	 * constant SYMM_TRANSFORMATION
	 * 
	 * @param plainText
	 *            message which will be encrypted
	 * @param key
	 *            symmetric key which is used for en- and decryption
	 * @return ciphertext which is the result of the encryption
	 */
	byte[] encryptSymmetric(byte[] plainText, byte[] key) {
		return encryptSymmetric(plainText, key, new byte[0]);
	}
	
	/**
	 * Returns the encrypted byte[] of the given plaintext, i.e.
	 * ciphertext=enc(plaintext,key,IV). The algorithm, mode and padding is
	 * set in constant SYMM_TRANSFORMATION. IV=(nonce||counter) 
	 * 
	 * @param plainText
	 *		message which will be encrypted
	 * @param key
	 *		symmetric key which is used for en- and decryption
	 * @param nonce
	 * 		random input that is concatenated to a counter
	 * @return ciphertext which is the result of the encryption
	 */

	synchronized byte[] encryptSymmetric(byte[] plainText, byte[] key, byte[] nonce) {
		ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
		SecretKeySpec symmetricKey = new SecretKeySpec(key, SYMM_KEY_ALGORITHM);
		ByteArrayOutputStream ivOS = new ByteArrayOutputStream();
		IvParameterSpec iv;
		byte[] counter = new byte[(SYMM_IV_SIZE_BIT-SYMM_NONCE_SIZE_BIT)/8];
		
		if(nonce == null || nonce.length != SYMM_NONCE_SIZE_BIT / 8) {
			nonce = getRandomBytes(SYMM_NONCE_SIZE_BIT / 8);
		}
		
		// Set counter to 1
		if(SYMM_IV_SIZE_BIT-SYMM_NONCE_SIZE_BIT > 0) {
			counter[counter.length-1] = 1;
		}
		
		try {
			ivOS.write(nonce);
			ivOS.write(counter);
			cipherText.write(nonce);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		iv = new IvParameterSpec(ivOS.toByteArray());

		try {
			symmetricCipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
			cipherText.write(symmetricCipher.doFinal(plainText));
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cipherText.toByteArray();
	}

	/**
	 * Returns the plaintext of the encrypted input
	 * plaintext=enc⁻¹(ciphertext,key) The algorithm, mode and padding is set in
	 * constant SYMM_TRANSFORMATION
	 * 
	 * @param cipherText
	 *            encrypted message which will be decrypted
	 * @param key
	 *            symmetric key which is used for en- and decryption
	 * @return plaintext which is the result of the decryption
	 */
	synchronized byte[] decryptSymmetric(byte[] cipherText, byte[] key) {
		ByteArrayInputStream bi = new ByteArrayInputStream(cipherText);
		byte[] nonce = new byte[SYMM_NONCE_SIZE_BIT / 8];
		byte[] counter = new byte[(SYMM_IV_SIZE_BIT-SYMM_NONCE_SIZE_BIT)/8];
		byte[] encryptedPlainText = new byte[cipherText.length
				- SYMM_NONCE_SIZE_BIT / 8];
		byte[] plainText = null;
		ByteArrayOutputStream ivOS = new ByteArrayOutputStream();
		IvParameterSpec iv;
		SecretKeySpec symmetricKey;

		// Set counter to 1
		if(SYMM_IV_SIZE_BIT-SYMM_NONCE_SIZE_BIT > 0) {
			counter[counter.length-1] = 1;
		}
		
		try {
			bi.read(nonce);
			ivOS.write(nonce);
			ivOS.write(counter);
			bi.read(encryptedPlainText);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		iv = new IvParameterSpec(ivOS.toByteArray());
		symmetricKey = new SecretKeySpec(key, SYMM_KEY_ALGORITHM);

		try {
			symmetricCipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
			plainText = symmetricCipher.doFinal(encryptedPlainText);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plainText;
	}

	/**
	 * Hybrid encrypts a String message for a recipient. The String message is
	 * encrypted with a random AES key. The AES key gets RSA encrypted with the
	 * recipients public key. The cipher text gets signed.
	 * 
	 * @param message
	 *            String message to encrypt
	 * @param recipient
	 *            Recipient to encrypt message for
	 * @param signatureKey
	 *            private key to sign message with
	 * 
	 * @return hybrid encrypted String message
	 * @throws InvalidKeyException
	 */
	public synchronized byte[] encryptHybridAndSign(String message,
			QblEncPublicKey recipient, QblSignKeyPair signatureKey)
			throws InvalidKeyException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		byte[] aesKey = getRandomBytes(AES_KEY_SIZE_BYTE);

		try {
			bs.write(rsaEncryptForRecipient(aesKey, recipient));
			bs.write(encryptSymmetric(message.getBytes(), aesKey));
			bs.write(createSignature(bs.toByteArray(), signatureKey));
		} catch (IOException e) {
			logger.error("IOException while writing to ByteArrayOutputStream");
		}
		return bs.toByteArray();
	}

	/**
	 * Decrypts a hybrid encrypted String message. Before decryption, the
	 * signature over the cipher text gets validated. The AES key is decrypted
	 * using the own private key. The decrypted AES key is used to decrypt the
	 * String message
	 * 
	 * @param cipherText
	 *            hybrid encrypted String message
	 * @param privKey
	 *            private key to encrypt String message with
	 * @param signatureKey
	 *            public key to validate signature with
	 * @return decrypted String message or null if message is undecryptable or
	 *         signature is invalid
	 * @throws InvalidKeyException
	 */
	public synchronized String decryptHybridAndValidateSignature(
			byte[] cipherText, QblPrimaryKeyPair privKey,
			QblSignPublicKey signatureKey) throws InvalidKeyException {
		ByteArrayInputStream bs = new ByteArrayInputStream(cipherText);
		// TODO: Include header byte

		if (bs.available() < RSA_SIGNATURE_SIZE_BYTE) {
			logger.debug("Avaliable data is less than RSA signature size!");
			return null;
		}
		// Get RSA encrypted AES key and encrypted data and signature over the
		// RSA
		// encrypted AES key and encrypted data
		byte[] encryptedMessage = new byte[bs.available()
				- RSA_SIGNATURE_SIZE_BYTE];
		byte[] rsaSignature = new byte[RSA_SIGNATURE_SIZE_BYTE];
		try {
			bs.read(encryptedMessage);
			bs.read(rsaSignature);
		} catch (IOException e) {
			logger.error("IOException while reading from ByteArrayInputStream");
		}

		// Validate signature over RSA encrypted AES key and encrypted data
		if (!validateSignature(encryptedMessage, rsaSignature, signatureKey)) {
			logger.debug("Message signature invalid!");
			return null;
		}

		// Read RSA encrypted AES key and encryptedData
		bs = new ByteArrayInputStream(encryptedMessage);

		byte[] encryptedAesKey = new byte[ENCRYPTED_AES_KEY_SIZE_BYTE];

		if (bs.available() < ENCRYPTED_AES_KEY_SIZE_BYTE) {
			logger.debug("Avaliable data is less than encrypted AES key size");
			return null;
		}
		byte[] aesCipherText = new byte[bs.available()
				- ENCRYPTED_AES_KEY_SIZE_BYTE];

		try {
			bs.read(encryptedAesKey);
			bs.read(aesCipherText);
		} catch (IOException e) {
			logger.error("IOException while reading from ByteArrayInputStream");
			e.printStackTrace();
		}

		// Decrypt RSA encrypted AES key and decrypt encrypted data with AES key
		byte[] aesKey = rsaDecrypt(encryptedAesKey,
				privKey.getQblEncPrivateKey());
		if (aesKey != null) {
			logger.debug("Message is OK!");
			return new String(decryptSymmetric(aesCipherText, aesKey));
		}
		return null;
	}

	/**
	 * Calculates HMAC of input.
	 * 
	 * @param text
	 *            input text
	 * @param key
	 *            key for HMAC calculation
	 * @return HMAC of text under key
	 */
	public synchronized byte[] calcHmac(byte[] text, byte[] key) {
		byte[] result = null;
		try {
			hmac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
			result = hmac.doFinal(text);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Simple verification of HMAC
	 * 
	 * @param text
	 *            original input text
	 * @param hmac
	 *            HMAC which will be verified
	 * @param key
	 *            key for HMAC calculation
	 * @return result of verification i.e. true/false
	 */
	public synchronized boolean validateHmac(byte[] text, byte[] hmac,
			byte[] key) {
		boolean validation = MessageDigest.isEqual(hmac, calcHmac(text, key));
		if (!validation) {
			logger.debug("HMAC is invalid!");
		}
		return validation;
	}
	
	/**
	 * Encryptes plaintext in GCM Mode to get an authenticated encryption.
	 * It's an alternative to encrypt-then-(H)MAC in CTR mode. It will be
	 * tested and reviewed which AE will be used.
	 * @param plainText
	 * 		Plaintext which will be encrypted 
	 * @param key
	 * 		Symmetric key which will be used for encryption and authentication
	 * @return
	 * 		Ciphertext, in format: IV|enc(plaintext)|authentication tag
	 */
	public synchronized byte[] encryptAuthenticatedSymmetric(byte[] plainText, byte[] key) {
		return encryptAuthenticatedSymmetric(plainText, key, null);
	}
	
	/**
	 * Encryptes plaintext in GCM Mode to get an authenticated encryption. It's
	 * an alternative to encrypt-then-(H)MAC in CTR mode. It will be tested and
	 * reviewed which AE will be used.
	 * 
	 * @param plainText
	 * 		Plaintext which will be encrypted 
	 * @param key
	 * 		Symmetric key which will be used for encryption and authentication
	 * @param nonce
	 * 		random input that is concatenated to a counter
	 * @return
	 * 		Ciphertext, in format: IV|enc(plaintext)|authentication tag
	 */
	synchronized byte[] encryptAuthenticatedSymmetric(byte[] plainText, byte[] key, byte[] nonce) {
		Cipher gcmCipher = null;
		SecretKeySpec symmetricKey;
		IvParameterSpec iv;
		ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
		
		try {
			gcmCipher = Cipher.getInstance(SYMM_ALT_TRANSFORMATION, CRYPTOGRAPHIC_PROVIDER);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(nonce == null || nonce.length != SYMM_NONCE_SIZE_BIT / 8) {
			nonce = getRandomBytes(SYMM_NONCE_SIZE_BIT / 8);
		}
		
		try {
			cipherText.write(nonce);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		iv = new IvParameterSpec(nonce);
		symmetricKey = new SecretKeySpec(key, SYMM_KEY_ALGORITHM);

		try {
			gcmCipher.init(Cipher.ENCRYPT_MODE, symmetricKey, iv);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			cipherText.write(gcmCipher.doFinal(plainText));
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cipherText.toByteArray();
	}

	/**
	 * Decryptes ciphertext in GCM Mode and verifies the integrity and
	 * authentication. As well as encryptAuthenticatedSymmetric() it will be
	 * tested which AE will be used.
	 * 
	 * @param cipherText
	 *            Ciphertext which will be decrypted
	 * @param key
	 * 			Symmetric key which will be used for decryption and verification
	 * @return
	 * 			Plaintext or null if validation of authentication tag fails
	 */

	public synchronized byte[] decryptAuthenticatedSymmetricAndValidateTag(
			byte[] cipherText, byte[] key) {
		ByteArrayInputStream bi = new ByteArrayInputStream(cipherText);
		byte[] nonce = new byte[SYMM_NONCE_SIZE_BIT / 8];
		byte[] encryptedPlainText = new byte[cipherText.length
				- SYMM_NONCE_SIZE_BIT / 8];
		byte[] plainText = null;
		IvParameterSpec iv;
		SecretKeySpec symmetricKey;

		try {
			gcmCipher = Cipher.getInstance(SYMM_ALT_TRANSFORMATION, CRYPTOGRAPHIC_PROVIDER);
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchProviderException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchPaddingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			bi.read(nonce);	
			bi.read(encryptedPlainText);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		iv = new IvParameterSpec(nonce);
		symmetricKey = new SecretKeySpec(key, SYMM_KEY_ALGORITHM);

		try {
			gcmCipher.init(Cipher.DECRYPT_MODE, symmetricKey, iv);
			plainText = gcmCipher.doFinal(encryptedPlainText);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO this exception is thrown if ciphertext or authentication tag
			// was modified
			logger.debug("Authentication tag is invalid!");
			return null;
		}
		return plainText;
	}
}
