package de.qabel.core.config;

import de.qabel.core.crypto.CryptoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

/**
 * Persistence defines methods to store encrypted entities into a database.
 * Entities has to be Serializable.
 * Serialized data is encrypted with AES256 GCM, a nonce provided to the serialize/deserialize
 * methods and a key derived from the provides password with PBKDF2 and a salt.
 *
 * Inheritors of Persistence must call init() when a call of getSalt() is possible. Usually after a database
 * connection is established.
 */
public abstract class Persistence {
	private final static Logger logger = LogManager.getLogger(Persistence.class.getName());
	private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final int PBKDF2_ROUNDS = 65536;
	private static final int AES_KEY_SIZE_BIT = 256;
	static final int AES_KEY_SIZE_BYTE = AES_KEY_SIZE_BIT / 8;
	static final int NONCE_SIZE_BYTE = 32;
	static final int SALT_SIZE_BYTE = 32;

	private boolean initCalled;
	private KeyParameter keyParameter;
	private SecretKeyFactory secretKeyFactory;
	CryptoUtils cryptoutils;

	/**
	 * Inheritors of Persistence must call init() when a call of getSalt() is possible. Usually after a database
	 * connection is established.
	 */
	public Persistence() {
		this.cryptoutils = new CryptoUtils();
		try {
			this.secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.fatal("Cannot find selected algorithm!", e);
			throw new RuntimeException("Cannot find selected algorithm!", e);
		}
		initCalled = false;
	}

	/**
	 * Initializes the persistence class
	 */
	final void init(char[] password) {
		if (password == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		this.keyParameter = getMasterKey(deriveKey(password, getSalt(false)));
		initCalled = true;
	}

	/**
	 * Checks if Persistence class has been initialized.
	 */
	private void checkInitCalled() {
		if (!initCalled) {
			throw new IllegalStateException("Inheritors of Persistence must call init() after establishing a connection!");
		}
	}

	/**
	 * Changes the encryption password by re-encrypting the master encryption key
	 * @param oldPassword Old password
	 * @param newPassword New password
	 * @return True if password has been changed
	 */
	final boolean changePassword(char[] oldPassword, char[] newPassword) {
		if (oldPassword == null || newPassword == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		return reEncryptMasterKey(deriveKey(oldPassword, getSalt(false)), deriveKey(newPassword, getSalt(true)));
	}

	/**
	 * Create new or receive previously created salt.
	 * @return salt for key derivation
	 */
	abstract byte[] getSalt(boolean forceNewSalt);

	/**
	 * Create new or receive previously created master encryption key.
	 * @return master encryption key
	 */
	abstract KeyParameter getMasterKey(KeyParameter encryptionKey);

	/**
	 * Re-encrypts the master key with a new encryption key
	 * @param oldKey Old encryption key
	 * @param newKey New encryption key
	 * @return True if master key has been re-encrypted
	 */
	abstract boolean reEncryptMasterKey(KeyParameter oldKey, KeyParameter newKey);

	/**
	 * Persists an entity
	 * @param id ID for entity storage
	 * @param object Entity to persist
	 * @return Result of the operation
	 */
	abstract boolean persistEntity(String id, Serializable object);

	/**
	 * Updated a previously stored entity
	 * @param id ID of the stored entity
	 * @param object Entity to replace stored entity with
	 * @return Result of the operation
	 */
	abstract boolean updateEntity(String id, Serializable object) throws IllegalArgumentException;

	/**
	 * Removes a persisted entity
	 * @param id ID of persisted entity
	 * @param cls Class of persisted entity
	 * @return Result of the operation
	 */
	abstract boolean removeEntity(String id, Class cls);

	/**
	 * Get an entity
	 * @param id ID of the stored entity
	 * @param cls Class of the entity to receive
	 * @return Stored entity or null if entity not found
	 */
	abstract Object getEntity(String id, Class cls);

	/**
	 * Get all entities of the provides Class
	 * @param cls Class to get all stored entities for
	 * @return List of stored entities
	 */
	abstract List<Object> getEntities(Class cls);

	/**
	 * Drops the table for the provided Class
	 * @param cls Class to drop table for
	 * @return Result of the operation
	 */
	abstract boolean dropTable(Class cls);

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
			key = secretKeyFactory.generateSecret(new PBEKeySpec(password, salt, PBKDF2_ROUNDS, AES_KEY_SIZE_BIT));
			return new KeyParameter(key.getEncoded());
		} catch (InvalidKeySpecException e) {
			logger.error("Invalid KeySpec!", e);
			throw new IllegalArgumentException("Cannot derive key from provided arguments!", e);
		}
	}

	/**
	 * Serialized a Serializable object into an encrypted byte array
	 * @param object Object to serialize
	 * @param nonce Nonce for encryption
	 * @return Encrypted serialized object
	 * @throws IllegalArgumentException
	 */
	byte[] serialize(String id, Serializable object, byte[] nonce) throws IllegalArgumentException {
		if (id == null || object == null || nonce == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		checkInitCalled();
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
	Object deserialize(String id, byte[] input, byte[] nonce) throws IllegalArgumentException {
		if(id == null || input == null || nonce == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if(id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		checkInitCalled();
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
