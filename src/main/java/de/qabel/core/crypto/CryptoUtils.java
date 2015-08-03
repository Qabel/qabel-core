package de.qabel.core.crypto;

import java.io.BufferedInputStream;
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
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.*;

public class CryptoUtils {

	// https://github.com/Qabel/qabel-doc/wiki/Components-Crypto
	private final static String CRYPTOGRAPHIC_PROVIDER = "BC"; // BouncyCastle
	private final static String SYMM_KEY_ALGORITHM = "AES";
	private final static String SYMM_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
	private final static int SYMM_GCM_READ_SIZE_BYTE = 4096; // Should be multiple of 4096 byte due to flash block size.
	private final static int SYMM_NONCE_SIZE_BYTE = 12;
	private final static int AES_KEY_SIZE_BYTE = 32;
	private final static int AES_KEY_SIZE_BIT = AES_KEY_SIZE_BYTE * 8;

	private static byte[] SUITE_NAME = "Noise255/AES256-GCM\0\0\0\0\0".getBytes();
	private static final String HMAC_ALGORITHM = "HMac/SHA512";
	private static final int H_LEN = 64;
	private static final int CV_LEN_BYTE = 48;
	private static final int SYMM_KEY_LEN_BYTE = 32;
	private static final int NONCE_LEN_BYTE = 12;
	private static final int MAC_BIT = 128;
	private static final int HEADER_CIPHER_TEXT_LEN_BYTE = 48;
	private static final int PADDING_LEN_BYTES = 4;
	public static final int ASYM_KEY_SIZE_BYTE = 32;

	private final static Logger logger = LoggerFactory.getLogger(CryptoUtils.class
			.getName());

	private SecureRandom secRandom;
	private Cipher gcmCipher;
	private Mac hmac;
	private KeyGenerator keyGenerator;

	public CryptoUtils() {

		try {
			Security.addProvider(new BouncyCastleProvider());

			secRandom = new SecureRandom();
			gcmCipher = Cipher.getInstance(SYMM_GCM_TRANSFORMATION,
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
	public byte[] getRandomBytes(int numBytes) {
		byte[] ranBytes = new byte[numBytes];
		secRandom.nextBytes(ranBytes);
		return ranBytes;
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
	 * @throws FileNotFoundException 
	 */
	public boolean encryptFileAuthenticatedSymmetric(File file, OutputStream outputStream, SecretKey key)
			throws InvalidKeyException, FileNotFoundException {
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
	 * @throws FileNotFoundException 
	 */
	public boolean encryptFileAuthenticatedSymmetric(File file, OutputStream outputStream, SecretKey key, byte[] nonce)
			throws InvalidKeyException, FileNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(file);
		return this.encryptStreamAuthenticatedSymmetric(fileInputStream, outputStream, key, nonce);
	}	
	
	/**
	 * Encrypts an InputStream to an OutputStream. The OutputStream gets the result
	 * immediately while encrypting. The step size of every separate decryption
	 * step is defined in SYMM_GCM_READ_SIZE_BYTE. Nonce of size
	 * SYMM_NONCE_SIZE_BIT is taken as nonce directly, else a random nonce is
	 * generated.
	 * 
	 * @param inputStream InputStream that will be encrypted
	 * @param outputStream OutputStream where ciphertext is streamed to
	 * @param key Key which is used to en-/decrypt
	 * @param nonce Random value which is concatenated to a counter
	 * @return true if encryption worked as expected, else false
	 * @throws InvalidKeyException if key is invalid
	 */
	public boolean encryptStreamAuthenticatedSymmetric(InputStream inputStream, OutputStream outputStream,
			SecretKey key, byte[] nonce) throws InvalidKeyException {
		IvParameterSpec iv;
		DataOutputStream cipherText = new DataOutputStream(outputStream);
		byte[] temp = new byte[SYMM_GCM_READ_SIZE_BYTE];
		int readBytes;

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
			while ((readBytes = inputStream.read(temp)) > 0) {
				cipherText.write(gcmCipher.update(temp, 0, readBytes));
			}
			cipherText.write(gcmCipher.doFinal());
			inputStream.close();
		} catch (IllegalBlockSizeException e) {
			// Should not happen
			logger.debug("Encryption: Block size of cipher was illegal => code mistake.", e);
		} catch (BadPaddingException e) {
			// We do not use padding, so this should not be thrown
			logger.error("Encryption: Bad padding", e);
		} catch (IOException e) {
			logger.debug("Encryption: Input/output Stream cannot be written/read to/from.", e);
			return false;
		}
		return true;
	}

	/**
	 * Decrypts ciphertext from an InputStream to a file. The decrypted content
	 * is written to the file immediately. If decryption was successful true
	 * is returned, if authentication tag validation fails or another error
	 * occurs false is returned.
	 * 
	 * @param inputStream
	 *            InputStream from where the ciphertext is read
	 * @param file
	 *            File in which the decrypted stream is stored
	 * @param key
	 *            Key which is used to en-/decrypt the file
	 * @return true if successfully decrypted or false if authentication tag validation
	 *         failed or another error occurred
	 * @throws InvalidKeyException
	 *             if key is invalid
	 * @throws IOException
	 */
	public boolean decryptFileAuthenticatedSymmetricAndValidateTag(InputStream inputStream, File file, SecretKey key)
			throws InvalidKeyException, IOException {
		byte[] nonce = new byte[SYMM_NONCE_SIZE_BYTE];
		IvParameterSpec iv;
		byte[] temp = new byte[SYMM_GCM_READ_SIZE_BYTE];
		BufferedInputStream bufferedInput = new BufferedInputStream(inputStream);
		int readBytes;

		try {
			bufferedInput.read(nonce);
		} catch (IOException e) {
			logger.debug("Decryption: Ciphertext (in this case the nonce) can not be read.", e);
			throw e;
		}

		iv = new IvParameterSpec(nonce);
		try {
			gcmCipher.init(Cipher.DECRYPT_MODE, key, iv);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException("Decryption: Wrong parameters for file decryption.", e);
		}
		
		FileOutputStream fileOutput = new FileOutputStream(file);
		try {
			while ((readBytes = bufferedInput.read(temp, 0,
					SYMM_GCM_READ_SIZE_BYTE)) > 0) {
				/*
				 * reading from a buffered input stream ensures that enough bytes
				 * are read to fulfill the block cipher min. length requirements.
				 */
				byte[] encBytes = gcmCipher.update(temp, 0, readBytes);
				if (encBytes == null) {
					logger.error("Input too short for block cipher. Input length was " + readBytes);
					throw new RuntimeException("Decryption failed due to unexpected input length.");
				}
				fileOutput.write(encBytes);
			}
			try {
				fileOutput.write(gcmCipher.doFinal());
			} catch (IllegalBlockSizeException e) {
				logger.debug("Decryption: File was encrypted with wrong block size.", e);
				// truncate file to avoid leakage of incomplete or unauthenticated data
				fileOutput.getChannel().truncate(0);
				return false;
			} catch (BadPaddingException e) {
				logger.error("Decryption: Authentication tag is invalid!", e);
				// truncate file to avoid leakage of incomplete or unauthenticated data
				fileOutput.getChannel().truncate(0);
				return false;
			}
		} finally {
			fileOutput.close();
		}

		return true;
	}
	
	/**
	 * Generates a new symmetric key for encryption.
	 * 
	 * @return new symmetric key.
	 */
	public SecretKey generateSymmetricKey() {
		return keyGenerator.generateKey();
	}

	/**
	 * Noise Key derivation function. Outputs a byte sequence that the caller typically splits into multiple variables
	 * such as a chain variable and cipher context, or two cipher contexts.
	 * @param secret secret for key derivation
	 * @param extraSecret is used to pass a chaining variable to mix into the KDF.
	 * @param info ensures that applying the KDF to the same secret values will produce independent output,
	 *             provided 'info' is different.
	 * @param outputLen length out the output
	 * @return derived key or null on unexpected errors
	 * @throws java.security.InvalidKeyException if secret cannot be used as a HMAC secret
	 */
	byte[] kdf(byte[] secret, byte[] extraSecret, byte[] info, int outputLen) throws InvalidKeyException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] t = new byte[H_LEN];

		// Not required to init the ByteArrayOutputStream with the
		// expected length, but it might improve the performance
		ByteArrayOutputStream bs = new ByteArrayOutputStream(info.length + 1 + 32 + extraSecret.length);
		for (int c = 0; c <= Math.ceil((double) outputLen / H_LEN) - 1; c++){
			try {
				bs.write(info);
				bs.write(c);
				bs.write(t, 0, 32);
				bs.write(extraSecret);
				hmac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
				t = Arrays.copyOfRange(hmac.doFinal(bs.toByteArray()), 0, H_LEN);
				outputStream.write(t);
				bs.reset();
			} catch (IOException e) {
				// Should never occur
				logger.error("Cannot write to ByteArrayOutputStream!", e);
				return null;
			}
		}
		return Arrays.copyOfRange(outputStream.toByteArray(), 0, outputLen);
	}

	/**
	 * Noise box is the structured anonymised encryption with the use of ECDH of
	 * receivers and an ephemeral key. Schematic:
	 * c = A' || enc(DH(A',B), A) || enc(DH(A,B)||DH(A',B), m)
	 * @param senderKey
	 * 		senders key pair
	 * @param targetPubKey
	 * 		receivers public key
	 * @param appData
	 * 		appData which will be decrypted
	 * @param padLen
	 * 	    length of padding added to the box. Negative values are ignored.
	 * @return
	 * 		ciphertext of mentioned format
	 * @throws java.security.InvalidKeyException
	 * 		if kdf cannot distribute a key from DH of given EC keys
	 */
	public byte[] createBox(QblECKeyPair senderKey, QblECPublicKey targetPubKey, byte[] appData, int padLen)
			throws InvalidKeyException {
		if (appData == null){
			appData = new byte[0];
		}

		QblECKeyPair ephKey = new QblECKeyPair();
		ByteArrayInputStream key1,key2;
		ByteArrayOutputStream header = new ByteArrayOutputStream();
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		ByteArrayOutputStream noiseBox = new ByteArrayOutputStream();
		ByteArrayOutputStream authtext = new ByteArrayOutputStream();
		ByteArrayOutputStream paddedPlaintext = new ByteArrayOutputStream(appData.length + PADDING_LEN_BYTES);
		byte[] cv1 = new byte[CV_LEN_BYTE];
		byte[] symmKey1 = new byte[SYMM_KEY_LEN_BYTE];
		byte[] nonce1 = new byte[NONCE_LEN_BYTE];
		byte[] symmKey2 = new byte[SYMM_KEY_LEN_BYTE];
		byte[] nonce2 = new byte[NONCE_LEN_BYTE];
		byte[] dh1 = ephKey.ECDH(targetPubKey);
		byte[] dh2 = senderKey.ECDH(targetPubKey);
		byte[] info = Arrays.copyOf(SUITE_NAME, SUITE_NAME.length + 1);
		try {
			// disable negative padding
			if (padLen < 0){
				padLen = 0;
			}
			byte[] encryptedPaddingLen = ByteBuffer.allocate(PADDING_LEN_BYTES).putInt(padLen).array();
			paddedPlaintext.write(appData);
			if (padLen > 0){
				paddedPlaintext.write(getRandomBytes(padLen));
			}
			paddedPlaintext.write(encryptedPaddingLen);

			// kdf
			key1 = new ByteArrayInputStream(kdf(dh1, new byte[CV_LEN_BYTE], info , CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE));
			key1.read(cv1);
			key1.read(symmKey1);
			key1.read(nonce1);
			info[info.length -1] += (byte) 0x01;

			// header = eph_key.pub || ENCRYPT(cc1, sender_key.pub, target_pubkey || eph_key.pub)
			authtext.write(targetPubKey.getKey());
			authtext.write(ephKey.getPub().getKey());

			header.write(ephKey.getPub().getKey());
			header.write(encrypt(new KeyParameter(symmKey1), nonce1, senderKey.getPub().getKey(), authtext.toByteArray()));

			// body = noise_body(cc2, appData, target_pubkey || header)
			key2 = new ByteArrayInputStream(kdf(dh2, cv1, info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE));
			key2.skip(CV_LEN_BYTE);	// Not used, so discarded
			key2.read(symmKey2);
			key2.read(nonce2);
			authtext.reset();
			authtext.write(targetPubKey.getKey());
			header.writeTo(authtext);
			body.write(encrypt(new KeyParameter(symmKey2), nonce2, paddedPlaintext.toByteArray(), authtext.toByteArray()));

			// noise box = header || body
			header.writeTo(noiseBox);
			body.writeTo(noiseBox);
		} catch (IOException e) {
			// Should never occur
			logger.error("Cannot write to ByteArrayOutputStream!", e);
			throw new RuntimeException(e);
		} catch (InvalidCipherTextException e) {
			// Should never occur
			logger.error("Unknown encryption error!", e);
			throw new RuntimeException(e);
		}
		return noiseBox.toByteArray();
	}

	/**
	 * Gets the plain content from a received noise box.
	 * @param targetKey
	 * 		receivers EC key pair
	 * @param noiseBox
	 * 		ciphertext which is received
	 * @return
	 * 		plaintext which is the content of the received noise box
	 * @throws java.security.InvalidKeyException
	 * 		if kdf cannot distribute a key from DH of given EC keys
	 * @throws org.bouncycastle.crypto.InvalidCipherTextException
	 * 		on decryption errors
	 */
	public DecryptedPlaintext readBox(QblECKeyPair targetKey, byte[] noiseBox) throws InvalidKeyException, InvalidCipherTextException {
		ByteArrayInputStream key1,key2;
		ByteArrayInputStream cipherStream = new ByteArrayInputStream(noiseBox);
		ByteArrayOutputStream authtext = new ByteArrayOutputStream();
		QblECPublicKey senderKey;
		int encryptedPaddingLength;
		byte[] ephRawKey = new byte[QblECPublicKey.KEY_SIZE_BYTE];
		byte[] cv1 = new byte[CV_LEN_BYTE];
		byte[] symmKey1 = new byte[SYMM_KEY_LEN_BYTE];
		byte[] nonce1 = new byte[NONCE_LEN_BYTE];
		byte[] symmKey2 = new byte[SYMM_KEY_LEN_BYTE];
		byte[] nonce2 = new byte[NONCE_LEN_BYTE];
		byte[] headerCipherText = new byte[HEADER_CIPHER_TEXT_LEN_BYTE];
		byte[] paddedPlaintext;
		byte[] bodyCipherText;
		byte[] info = Arrays.copyOf(SUITE_NAME, SUITE_NAME.length + 1);
		try {
			// read ephKey
			if (cipherStream.read(ephRawKey) != ASYM_KEY_SIZE_BYTE){
				throw new InvalidCipherTextException("Invalid ephKey length!");
			}
			QblECPublicKey ephKey = new QblECPublicKey(ephRawKey);

			// first kdf
			byte[] dh1 = targetKey.ECDH(ephKey);
			key1 = new ByteArrayInputStream(kdf(dh1, new byte[CV_LEN_BYTE], info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE));
			if (key1.read(cv1) != CV_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid cv1 length!");
			}
			if (key1.read(symmKey1) != SYMM_KEY_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid symmKey length!");
			}
			if (key1.read(nonce1) != NONCE_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid nonce1 length!");
			}

			// sender_key.pub = DECRYPT(cc1, header_cipher_text, target_pubkey || eph_key.pub)
			authtext.write(targetKey.getPub().getKey());
			authtext.write(ephRawKey);
			if (cipherStream.read(headerCipherText) != HEADER_CIPHER_TEXT_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid headerCipherText length!");
			}

			byte[] senderRawKey = decrypt(new KeyParameter(symmKey1), nonce1, headerCipherText, authtext.toByteArray());
			senderKey = new QblECPublicKey(senderRawKey);

			// second kdf
			byte[] dh2 = targetKey.ECDH(senderKey);
			info[info.length -1] += (byte) 0x01;
			key2 = new ByteArrayInputStream(kdf(dh2, cv1, info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE));
			if (key2.skip(CV_LEN_BYTE) != CV_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid cv2 length!");
			}
			if (key2.read(symmKey2) != SYMM_KEY_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid symmKey2 length!");
			}
			if (key2.read(nonce2) != NONCE_LEN_BYTE){
				throw new InvalidCipherTextException("Invalid nonce2 length!");
			}

			// plaintext = noise_body^-1(cc2, body, target_pubkey || header)
			authtext.reset();
			authtext.write(targetKey.getPub().getKey());
			authtext.write(ephRawKey);
			authtext.write(headerCipherText);
			bodyCipherText = new byte[cipherStream.available()];
			cipherStream.read(bodyCipherText);

		} catch (IOException e) {
			throw new InvalidCipherTextException("Invalid ciphertext!");
		}

		paddedPlaintext = decrypt(new KeyParameter(symmKey2), nonce2, bodyCipherText, authtext.toByteArray());
		encryptedPaddingLength = ByteBuffer.wrap(
				Arrays.copyOfRange(paddedPlaintext, paddedPlaintext.length - PADDING_LEN_BYTES, paddedPlaintext.length)).getInt();

		// Validate padding length
		if (encryptedPaddingLength < 0 || encryptedPaddingLength > paddedPlaintext.length - PADDING_LEN_BYTES){
			throw new InvalidCipherTextException("Invalid padding length!");
		}
		return new DecryptedPlaintext(senderKey,
				Arrays.copyOfRange(paddedPlaintext, 0, paddedPlaintext.length - PADDING_LEN_BYTES - encryptedPaddingLength));
	}


	/**
	 * Encrypts a plaintext with associated data with AES GCM
	 * @param key encryption key
	 * @param nonce nonce for encryption
	 * @param plaintext plaintext to encrypt
	 * @param associatedData additionally associated data
	 * @return encrypted plaintext
	 * @throws org.bouncycastle.crypto.InvalidCipherTextException on encryption errors
	 */
	public byte[] encrypt(KeyParameter key, byte[] nonce, byte[] plaintext, byte[] associatedData) throws InvalidCipherTextException {
		AEADParameters params = new AEADParameters(key, MAC_BIT, nonce, associatedData);
		GCMBlockCipher gcm = new GCMBlockCipher(new AESEngine());
		gcm.init(true, params);

		byte[] output = new byte[gcm.getOutputSize(plaintext.length)];
		int offOut = gcm.processBytes(plaintext, 0, plaintext.length, output, 0);
		gcm.doFinal(output, offOut);
		return output;
	}

	/**
	 * Decrypts a ciphertext with associated data with AES GCM
	 * @param key encryption key
	 * @param nonce nonce for encryption
	 * @param ciphertext ciphertext to encrypt
	 * @param associatedData additionally associated data
	 * @return encrypted ciphertext
	 * @throws org.bouncycastle.crypto.InvalidCipherTextException on decryption errors
	 */
	public byte[] decrypt(KeyParameter key, byte[] nonce, byte[] ciphertext, byte[] associatedData) throws InvalidCipherTextException {
		AEADParameters params = new AEADParameters(key, MAC_BIT, nonce, associatedData);
		GCMBlockCipher gcm = new GCMBlockCipher(new AESEngine());
		gcm.init(false, params);

		byte[] output = new byte[gcm.getOutputSize(ciphertext.length)];
		int offOut = gcm.processBytes(ciphertext, 0, ciphertext.length, output, 0);
		gcm.doFinal(output, offOut);
		return output;
	}
}
