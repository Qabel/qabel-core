package de.qabel.core.config;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.params.KeyParameter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Persistence defines methods to store encrypted entities into a database.
 * Entities has to be Persistable.
 * Serialized data is encrypted with AES256 GCM, a nonce provided to the serialize/deserialize
 * methods and a key derived from the provides password with PBKDF2 and a salt.
 *
 */
public abstract class EncryptedPersistence<T> extends Persistence<T> {
	private final static Logger logger = LoggerFactory.getLogger(EncryptedPersistence.class.getName());
	private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	//TODO: Find a reasonable default value
	private static final int NUM_DEFAULT_PBKDF2_ROUNDS = 65536;
	private static final int AES_KEY_SIZE_BIT = 256;

	private KeyParameter keyParameter;
	private SecretKeyFactory secretKeyFactory;
	private final int pbkdf2Rounds;

	protected static final int AES_KEY_SIZE_BYTE = AES_KEY_SIZE_BIT / 8;
	protected static final int NONCE_SIZE_BYTE = 32;
	protected static final int SALT_SIZE_BYTE = 32;

	protected CryptoUtils cryptoutils;

	/**
	 * Construct Persistence with default PBKDF2 rounds.
	 * @param database Generic database object. Used for generic connect(T) method.
	 * @param password Password to encrypt database with.
	 * @throws QblInvalidEncryptionKeyException
	 */
	public EncryptedPersistence(T database, char[] password) throws QblInvalidEncryptionKeyException {
		this(database, password, NUM_DEFAULT_PBKDF2_ROUNDS);
	}

	/**
	 * Construct Persistence.
	 * @param database Generic database object. Used for generic connect(T) method.
	 * @param password Password to encrypt database with.
	 * @param numPBKDF2rounds Number of PBKDF2 rounds.
	 * @throws QblInvalidEncryptionKeyException
	 */
	public EncryptedPersistence(T database, char[] password, int numPBKDF2rounds) throws QblInvalidEncryptionKeyException {
		this.cryptoutils = new CryptoUtils();
		this.pbkdf2Rounds = numPBKDF2rounds;
		try {
			this.secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Cannot find selected algorithm!", e);
			throw new RuntimeException("Cannot find selected algorithm!", e);
		}
		if (!connect(database)) {
			logger.error("Cannot connect to database!");
			throw new RuntimeException("Cannot connect to database!");
		}
		this.keyParameter = getMasterKey(deriveKey(password, getSalt(false)));
		if (this.keyParameter == null) {
			throw new QblInvalidEncryptionKeyException();
		}
	}

	/**
	 * Changes the encryption password by re-encrypting the master encryption key
	 * @param oldPassword Old password
	 * @param newPassword New password
	 * @return True if password has been changed
	 */
	final public boolean changePassword(char[] oldPassword, char[] newPassword) {
		return reEncryptMasterKey(deriveKey(oldPassword, getSalt(false)), deriveKey(newPassword, getSalt(true)));
	}

	/**
	 * Create new or receive previously created salt.
	 * @return salt for key derivation
	 */
	protected abstract byte[] getSalt(boolean forceNewSalt);

	/**
	 * Create new or receive previously created master encryption key.
	 * @return master encryption key
	 */
	protected abstract KeyParameter getMasterKey(KeyParameter encryptionKey);

	/**
	 * Re-encrypts the master key with a new encryption key
	 * @param oldKey Old encryption key
	 * @param newKey New encryption key
	 * @return True if master key has been re-encrypted
	 */
	protected abstract boolean reEncryptMasterKey(KeyParameter oldKey, KeyParameter newKey);

	/**
	 * Derives the encryption key from the password and a salt.
 	 * @param password Password for key derivation
	 * @param salt Salt for key derivation
	 * @return Encryption key for serialization/deserialization
	 */
	private KeyParameter deriveKey(char[] password, byte[] salt) throws IllegalArgumentException {
		if (password == null || salt == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		SecretKey key;
		try {
			key = secretKeyFactory.generateSecret(new PBEKeySpec(password, salt, pbkdf2Rounds, AES_KEY_SIZE_BIT));
			return new KeyParameter(key.getEncoded());
		} catch (InvalidKeySpecException e) {
			logger.error("Invalid KeySpec!", e);
			throw new IllegalArgumentException("Cannot derive key from provided arguments!", e);
		}
	}

	/**
	 * Serializes a Serializable object into an encrypted byte array
	 * @param object Object to serialize
	 * @param nonce Nonce for encryption
	 * @return Encrypted serialized object
	 * @throws IllegalArgumentException
	 */
	protected byte[] serialize(String id, Serializable object, byte[] nonce) throws IllegalArgumentException {
		if (id == null || object == null || nonce == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] encryptedObject;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.close();
			encryptedObject = cryptoutils.encrypt(keyParameter, nonce, baos.toByteArray(), id.getBytes());
		} catch (InvalidCipherTextException | IOException e) {
			throw new IllegalArgumentException("Cannot serialize object!", e);
		}
		return encryptedObject;
	}

	/**
	 * Deserializes an encrypted object
	 * @param input Encrypted byte array to deserialize
	 * @param nonce Nonce for decryption
	 * @return Deserialized object
	 * @throws IllegalArgumentException
	 */
	protected Object deserialize(String id, byte[] input, byte[] nonce) throws IllegalArgumentException {
		if(id == null || input == null || nonce == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if(id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		Object decryptedObject;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(cryptoutils.decrypt(keyParameter, nonce, input, id.getBytes()));
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				decryptedObject = ois.readObject();
			}
		} catch (InvalidCipherTextException | ClassNotFoundException | IOException e) {
			throw new IllegalArgumentException("Cannot deserialize object!", e);
		}
		return decryptedObject;
	}

}
