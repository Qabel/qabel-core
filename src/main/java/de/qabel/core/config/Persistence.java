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
	CryptoUtils cryptoutils;

	/**
	 * Inheritors of Persistence must call init() when a call of getSalt() is possible. Usually after a database
	 * connection is established.
	 */
	public Persistence() {
		this.cryptoutils = new CryptoUtils();
		initCalled = false;
	}

	/**
	 * Initializes the persistence class
	 */
	final void init(String password) {
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
	final boolean changePassword(String oldPassword, String newPassword) {
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
	abstract boolean updateEntity(String id, Serializable object);

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
	 * @param cls Class of the entity to reveice
	 * @return Stored entity
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
	private KeyParameter deriveKey(String password, byte[] salt) {
		SecretKey key;
		try {
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
			key = secretKeyFactory.generateSecret(
					new PBEKeySpec(password.toCharArray(), salt,
							PBKDF2_ROUNDS, AES_KEY_SIZE_BIT));
			return new KeyParameter(key.getEncoded());
		} catch (InvalidKeySpecException e) {
			logger.error("Invalid KeySpec!", e);
			return null;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Invalid algorithm!", e);
			return null;
		}
	}

	/**
	 * Serialized a Serializable object into an encrypted byte array
	 * @param object Object to serialize
	 * @param nonce Nonce for encryption
	 * @return Encrypted serialized object
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidCipherTextException
	 */
	byte[] serialize(Serializable object, byte[] nonce) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidCipherTextException {
		checkInitCalled();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);
		oos.close();
		return cryptoutils.encrypt(keyParameter, nonce, baos.toByteArray(), null);
	}

	/**
	 * Deserializes an encrypted object
	 * @param input Encrypted byte array to deserialize
	 * @param nonce Nonce for decryption
	 * @return Deserialized object
	 * @throws InvalidCipherTextException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	Object deserialize(byte[] input, byte[] nonce) throws InvalidCipherTextException, IOException, ClassNotFoundException {
		checkInitCalled();
		ByteArrayInputStream bais = new ByteArrayInputStream(cryptoutils.decrypt(keyParameter, nonce, input, null));
		try (ObjectInputStream ois = new ObjectInputStream(bais)) {
			return ois.readObject();
		}
	}
}
