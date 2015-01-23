package de.qabel.core.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.*;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtils {

	private final static String CRYPTOGRAPHIC_PROVIDER = "BC"; // BouncyCastle
	// https://github.com/Qabel/qabel-doc/wiki/Components-Crypto
	private final static String MESSAGE_DIGEST_ALGORITHM = "SHA-512";
	private final static String SIGNATURE_ALGORITHM = "RSASSA-PSS";
	private final static String RSA_CIPHER_ALGORITM = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";
	private final static String HMAC_ALGORITHM = "HMac/" + "SHA512";
	final static int RSA_SIGNATURE_SIZE_BYTE = 256;
	final static String SYMM_KEY_ALGORITHM = "AES";
	private final static String SYMM_TRANSFORMATION = "AES/CTR/NoPadding";
	private final static String SYMM_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
	private final static int SYMM_GCM_READ_SIZE_BYTE = 4096; // Should be multiple of 4096 byte due to flash block size.
	private final static int SYMM_IV_SIZE_BYTE = 16;
	final static int SYMM_NONCE_SIZE_BYTE = 12;
	private final static int AES_KEY_SIZE_BYTE = 32;
	private final static int AES_KEY_SIZE_BIT = AES_KEY_SIZE_BYTE * 8;
	final static int ENCRYPTED_AES_KEY_SIZE_BYTE = 256;

	private final static Logger logger = LogManager.getLogger(CryptoUtils.class
			.getName());

	private SecureRandom secRandom;
	private MessageDigest messageDigest;
	private Cipher symmetricCipher;
	private Cipher asymmetricCipher;
	private Cipher gcmCipher;
	private Signature signer;
	private Mac hmac;
	private KeyGenerator keyGenerator;

	public CryptoUtils() {

		try {
			Security.addProvider(new BouncyCastleProvider());

			secRandom = new SecureRandom();
			messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			symmetricCipher = Cipher.getInstance(SYMM_TRANSFORMATION,
					CRYPTOGRAPHIC_PROVIDER);
			asymmetricCipher = Cipher.getInstance(RSA_CIPHER_ALGORITM,
					CRYPTOGRAPHIC_PROVIDER);
			gcmCipher = Cipher.getInstance(SYMM_GCM_TRANSFORMATION,
					CRYPTOGRAPHIC_PROVIDER);
			signer = Signature.getInstance(SIGNATURE_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			hmac = Mac.getInstance(HMAC_ALGORITHM, CRYPTOGRAPHIC_PROVIDER);
			keyGenerator = KeyGenerator.getInstance(SYMM_KEY_ALGORITHM,
					CRYPTOGRAPHIC_PROVIDER);
			keyGenerator.init(AES_KEY_SIZE_BIT);
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

	/**
	 * Returns a random byte array with an arbitrary size
	 * 
	 * @param numBytes
	 *            Number of random bytes
	 * @return byte[ ] with random bytes
	 */
	byte[] getRandomBytes(int numBytes) {
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
	public byte[] getSHA512sum(byte[] bytes) {
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
	public String getSHA512sumHumanReadable(byte[] bytes) {
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
	public byte[] getSHA512sum(String plain) {
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
	public String getSHA512sumHumanReadable(String plain) {
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
	byte[] createSignature(byte[] message, QblSignKeyPair signatureKey) {
		byte[] sha512Sum = getSHA512sum(message);
		return rsaSign(sha512Sum, signatureKey);
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
	 * @return Signature of data. Can be null if error occurred.
	 */
	byte[] rsaSign(byte[] data, RSAPrivateKey signatureKey) {
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
	boolean rsaValidateSignature(byte[] data, byte[] signature,
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
	boolean validateSignature(byte[] message, byte[] signature,
			QblSignPublicKey signPublicKey) throws InvalidKeyException {
		byte[] sha512Sum = getSHA512sum(message);
		return rsaValidateSignature(sha512Sum, signature,
				signPublicKey.getRSAPublicKey());
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
	byte[] rsaEncryptForRecipient(byte[] message,
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
	byte[] rsaDecrypt(byte[] cipherText, RSAPrivateKey privKey)
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
	 * constant SYMM_TRANSFORMATION. A random value is used for the nonce.
	 * 
	 * @param plainText
	 *            message which will be encrypted
	 * @param key
	 *            symmetric key which is used for en- and decryption
	 * @return ciphertext which is the result of the encryption
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	byte[] encryptSymmetric(byte[] plainText, SecretKey key) throws InvalidKeyException {
		return encryptSymmetric(plainText, key, null);
	}

	/**
	 * Returns the encrypted byte[] of the given plaintext, i.e.
	 * ciphertext=enc(plaintext,key,IV). The algorithm, mode and padding is set
	 * in constant SYMM_TRANSFORMATION. IV=(nonce||counter)
	 * 
	 * @param plainText
	 *            message which will be encrypted
	 * @param key
	 *            symmetric key which is used for en- and decryption
	 * @param nonce
	 *            random input that is concatenated to a counter
	 * @return ciphertext which is the result of the encryption
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	byte[] encryptSymmetric(byte[] plainText, SecretKey key, byte[] nonce) throws InvalidKeyException {
		ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
		ByteArrayOutputStream ivOS = new ByteArrayOutputStream();
		IvParameterSpec iv;
		byte[] counter = new byte[(SYMM_IV_SIZE_BYTE - SYMM_NONCE_SIZE_BYTE)];

		if (nonce == null || nonce.length != SYMM_NONCE_SIZE_BYTE) {
			nonce = getRandomBytes(SYMM_NONCE_SIZE_BYTE);
		}

		// Set counter to 1, if nonce is smaller than IV
		if (SYMM_IV_SIZE_BYTE - SYMM_NONCE_SIZE_BYTE > 0) {
			counter[counter.length - 1] = 1;
		}

		try {
			ivOS.write(nonce);
			ivOS.write(counter);
			cipherText.write(nonce);
		} catch (IOException e) {
			logger.error("Encryption: Nonce cannot be written to the ciphertext stream: ", e);
		}

		iv = new IvParameterSpec(ivOS.toByteArray());

		try {
			symmetricCipher.init(Cipher.ENCRYPT_MODE, key, iv);
			cipherText.write(symmetricCipher.doFinal(plainText));
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug("Encryption: Wrong parameters for file encryption.", e);
		} catch (IllegalBlockSizeException e) {
			// CTR means stream cipher, so this should not be thrown
			logger.error(e);
		} catch (BadPaddingException e) {
			// We do not use padding, so this should not be thrown
			logger.error(e);
		} catch (IOException e) {
			logger.debug("Encryption: Output Stream cannot be written to.", e);
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
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	byte[] decryptSymmetric(byte[] cipherText, SecretKey key) throws InvalidKeyException {
		ByteArrayInputStream bi = new ByteArrayInputStream(cipherText);
		byte[] nonce = new byte[SYMM_NONCE_SIZE_BYTE];
		byte[] counter = new byte[(SYMM_IV_SIZE_BYTE - SYMM_NONCE_SIZE_BYTE)];
		byte[] encryptedPlainText = new byte[cipherText.length
				- SYMM_NONCE_SIZE_BYTE];
		byte[] plainText = null;
		ByteArrayOutputStream ivOS = new ByteArrayOutputStream();
		IvParameterSpec iv;

		// Set counter to 1, if nonce is smaller than IV
		if (SYMM_IV_SIZE_BYTE - SYMM_NONCE_SIZE_BYTE > 0) {
			counter[counter.length - 1] = 1;
		}

		try {
			bi.read(nonce);
			ivOS.write(nonce);
			ivOS.write(counter);
			bi.read(encryptedPlainText);
		} catch (IOException e) {
			logger.error("Decryption: Ciphertext could not be read: ", e);
		}

		iv = new IvParameterSpec(ivOS.toByteArray());

		try {
			symmetricCipher.init(Cipher.DECRYPT_MODE, key, iv);
			plainText = symmetricCipher.doFinal(encryptedPlainText);
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug("Decryption: Wrong parameters for decryption.", e);
		} catch (IllegalBlockSizeException e) {
			// CTR mode means stream cipher, so this should not be thrown
			logger.error(e);
		} catch (BadPaddingException e) {
			// We do not use padding, so this should not be thrown
			logger.error(e);
		}
		return plainText;
	}
	
	/**
	 * Hybrid encrypts a String message for a recipient. The String message is
	 * encrypted with a random AES key. The AES key gets RSA encrypted with the
	 * recipients public key. The cipher text gets signed.
	 * 
	 * This function is deprecated. Use an AbstractBinaryDropMessage instead.
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
	@Deprecated
	public byte[] encryptHybridAndSign(String message,
			QblEncPublicKey recipient, QblSignKeyPair signatureKey)
			throws InvalidKeyException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		SecretKey aesKey = keyGenerator.generateKey();
		
		// pad message

		try {
			bs.write(rsaEncryptForRecipient(aesKey.getEncoded(), recipient));
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
	 * This function is deprecated. Use an AbstractBinaryDropMessage instead.
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
	@Deprecated
	public String decryptHybridAndValidateSignature(
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
		byte[] rawAesKey = rsaDecrypt(encryptedAesKey,
				privKey.getQblEncPrivateKey());
		if (rawAesKey != null) {
			logger.debug("Message is OK!");
			return new String(decryptSymmetric(aesCipherText, 
					new SecretKeySpec(rawAesKey, SYMM_KEY_ALGORITHM)));
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
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */

	public byte[] calcHmac(byte[] text, SecretKey key) throws InvalidKeyException {
		hmac.init(key);
		return hmac.doFinal(text);
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
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	public boolean validateHmac(byte[] text, byte[] hmac, SecretKey key) throws InvalidKeyException {
		boolean validation = MessageDigest.isEqual(hmac, calcHmac(text, key));
		if (!validation) {
			logger.debug("HMAC is invalid!");
		}
		return validation;
	}

	/**
	 * Encryptes plaintext in GCM Mode to get an authenticated encryption. It's
	 * an alternative to encrypt-then-(H)MAC in CTR mode. A random value is used
	 * for the nonce.
	 * 
	 * @param plainText
	 *            Plaintext which will be encrypted
	 * @param key
	 *            Symmetric key which will be used for encryption and
	 *            authentication
	 * @return Ciphertext, in format: IV|enc(plaintext)|authentication tag or
	 *         null if an error occurs
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	public byte[] encryptAuthenticatedSymmetric(byte[] plainText, SecretKey key) throws InvalidKeyException {
		return encryptAuthenticatedSymmetric(plainText, key, null);
	}

	/**
	 * Encryptes plaintext in GCM Mode to get an authenticated encryption. It's
	 * an alternative to encrypt-then-(H)MAC in CTR mode. It will be tested and
	 * reviewed which AE will be used.
	 * 
	 * @param plainText
	 *            Plaintext which will be encrypted
	 * @param key
	 *            Symmetric key which will be used for encryption and
	 *            authentication
	 * @param nonce
	 *            random input that is concatenated to a counter
	 * @return Ciphertext, in format: IV|enc(plaintext)|authentication tag
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	public byte[] encryptAuthenticatedSymmetric(byte[] plainText,
			SecretKey key, byte[] nonce) throws InvalidKeyException {
		IvParameterSpec iv;
		ByteArrayOutputStream cipherText = new ByteArrayOutputStream();

		if (nonce == null || nonce.length != SYMM_NONCE_SIZE_BYTE) {
			nonce = getRandomBytes(SYMM_NONCE_SIZE_BYTE);
		}

		try {
			cipherText.write(nonce);
		} catch (IOException e) {
			// Should not happen since it is generated just before
			logger.error(e);
		}

		iv = new IvParameterSpec(nonce);

		try {
			gcmCipher.init(Cipher.ENCRYPT_MODE, key, iv);
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug("Encryption: Wrong parameters for encryption cipher.", e);
		}

		try {
			cipherText.write(gcmCipher.doFinal(plainText));
		} catch (IllegalBlockSizeException e) {
			logger.debug("Encryption: Block size of cipher was illegal => code mistake.", e);
		} catch (BadPaddingException e) {
			// We do not use padding , so this should not be thrown
			logger.error(e);
		} catch (IOException e) {
			// Will not happen since cipherText is not modified outside of this function
			logger.error(e);
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
	 *            Symmetric key which will be used for decryption and
	 *            verification
	 * @return Plaintext or null if validation of authentication tag fails or
	 *         another error occurs
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */

	public byte[] decryptAuthenticatedSymmetricAndValidateTag(
			byte[] cipherText, SecretKey key) throws InvalidKeyException {
		ByteArrayInputStream bi = new ByteArrayInputStream(cipherText);
		byte[] nonce = new byte[SYMM_NONCE_SIZE_BYTE];
		byte[] encryptedPlainText = new byte[cipherText.length
				- SYMM_NONCE_SIZE_BYTE];
		byte[] plainText = null;
		IvParameterSpec iv;

		try {
			bi.read(nonce);
			bi.read(encryptedPlainText);
		} catch (IOException e) {
			logger.debug("Decryption: Ciphertext can not be read.", e);
		}

		iv = new IvParameterSpec(nonce);

		try {
			gcmCipher.init(Cipher.DECRYPT_MODE, key, iv);
			plainText = gcmCipher.doFinal(encryptedPlainText);
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug("Decryption: Wrong parameters for decryption.", e);
			return null;
		} catch (IllegalBlockSizeException e) {
			logger.debug("Decryption: Ciphertext was encrypted with wrong block size.", e);
			return null;
		} catch (BadPaddingException e) {
			logger.error("Decryption: Authentication tag is invalid!", e);
			return null;
		}
		return plainText;
	}

	/**
	 * Encrypts a File to an OutputStream. The OutputStream gets the result
	 * immediately while encrypting. The step size of every seperate decryption
	 * step is defined in SYMM_ALT_READ_SIZE_BYTE.
	 * 
	 * @param file
	 *            Input file that will be encrypted
	 * @param outputStream
	 *            OutputStream where ciphertext is streamed to
	 * @param key
	 *            Key which is used to en-/decrypt
	 * @return true if encryption worked as expected, else false
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	boolean encryptFileAuthenticatedSymmetric(File file, OutputStream outputStream, SecretKey key)
			throws InvalidKeyException {
		return encryptFileAuthenticatedSymmetric(file, outputStream, key, null);
	}

	/**
	 * Encrypts a File to an OutputStream. The OutputStream gets the result
	 * immediately while encrypting. The step size of every seperate decryption
	 * step is defined in SYMM_ALT_READ_SIZE_BYTE. Nonce of size
	 * SYMM_NONCE_SIZE_BIT is taken as nonce directly, else a random nonce is
	 * generated.
	 * 
	 * @param file
	 *            Input file that will be encrypted
	 * @param outputStream
	 *            OutputStream where ciphertext is streamed to
	 * @param key
	 *            Key which is used to en-/decrypt
	 * @param nonce
	 *            Random value which is concatenated to a counter
	 * @return true if encryption worked as expected, else false
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	boolean encryptFileAuthenticatedSymmetric(File file, OutputStream outputStream, SecretKey key, byte[] nonce)
			throws InvalidKeyException {
		IvParameterSpec iv;
		DataOutputStream cipherText = new DataOutputStream(outputStream);
		FileInputStream fileInputStream;
		byte[] temp = new byte[SYMM_GCM_READ_SIZE_BYTE];
		int readBytes;

		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.debug("Encryption: File for encryption was not found.", e);
			return false;
		}

		if (nonce == null || nonce.length != SYMM_NONCE_SIZE_BYTE) {
			nonce = getRandomBytes(SYMM_NONCE_SIZE_BYTE);
		}

		iv = new IvParameterSpec(nonce);

		try {
			gcmCipher.init(Cipher.ENCRYPT_MODE, key, iv);
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug("Encryption: Wrong parameters for file encryption cipher.", e);
			return false;
		}

		try {
			cipherText.write(nonce);
			while ((readBytes = fileInputStream.read(temp, 0,
					SYMM_GCM_READ_SIZE_BYTE)) > 0) {
				cipherText.write(gcmCipher.update(temp, 0, readBytes));
			}
			cipherText.write(gcmCipher.doFinal());
			fileInputStream.close();
		} catch (IllegalBlockSizeException e) {
			// Should not happen
			logger.debug("Encryption: Block size of cipher was illegal => code mistake.", e);
		} catch (BadPaddingException e) {
			// We do not use padding, so this should not be thrown
			logger.error(e);
		} catch (IOException e) {
			logger.debug("Encryption: Input/output Stream cannot be written/read to/from.", e);
			return false;
		}
		return true;
	}

	/**
	 * Decryptes ciphertext from an InputStream to a file. The decrypted content
	 * is written to the file immediately. If decryption was successful a file
	 * is returned, if authentication tag validation fails or another error
	 * occurs null is returned.
	 * 
	 * @param inputStream
	 *            InputStream from where the ciphertext is read
	 * @param pathName
	 *            Pathname where the decrypted file will be stored
	 * @param key
	 *            Key which is used to en-/decrypt the file
	 * @return The decrypted file or null if authentication tag validation
	 *         failed or another error occured
	 * @throws InvalidKeyException
	 *             if key is invalid
	 */
	File decryptFileAuthenticatedSymmetricAndValidateTag(InputStream inputStream, String pathName, SecretKey key)
			throws InvalidKeyException {
		FileOutputStream fileOutput = null;
		byte[] nonce = new byte[SYMM_NONCE_SIZE_BYTE];
		IvParameterSpec iv;
		byte[] temp = new byte[SYMM_GCM_READ_SIZE_BYTE];
		int readBytes;

		if (pathName == null) {
			// TODO generate a temp folder where files can be stored
			logger.debug("TODO generate a temp folder where files can be stored");
		}

		try {
			fileOutput = new FileOutputStream(pathName);
		} catch (FileNotFoundException e) {
			logger.debug("Decryption: File " + pathName + " was not found/can not be written to.", e);
			// TODO like above: create temp file
		}

		try {
			inputStream.read(nonce);
		} catch (IOException e) {
			logger.debug("Decryption: Ciphertext (in this case the nonce) can not be read.", e);
			return null;
		}

		iv = new IvParameterSpec(nonce);
		try {
			gcmCipher.init(Cipher.DECRYPT_MODE, key, iv);
			while ((readBytes = inputStream.read(temp, 0,
					SYMM_GCM_READ_SIZE_BYTE)) > 0) {
				fileOutput.write(gcmCipher.update(temp, 0, readBytes));
			}
			fileOutput.write(gcmCipher.doFinal());
			fileOutput.close();
		} catch (InvalidAlgorithmParameterException e) {
			logger.debug("Decryption: Wrong parameters for file decryption.", e);
			return null;
		} catch (IllegalBlockSizeException e) {
			logger.debug("Decryption: File was encrypted with wrong block size.", e);
			return null;
		} catch (BadPaddingException e) {
			logger.error("Decryption: Authentication tag is invalid!", e);
			return null;
		} catch (IOException e) {
			logger.debug("Decryption: Input/output Stream cannot be written/read to/from.", e);
			return null;
		}

		return new File(pathName);
	}
	
	/**
	 * Generates a new symmetric key for encryption.
	 * 
	 * @return new symmetric key.
	 */
	public SecretKey generateSymmetricKey() {
		return keyGenerator.generateKey();
	}
}
