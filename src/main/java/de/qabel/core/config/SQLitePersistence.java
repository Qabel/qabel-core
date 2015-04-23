package de.qabel.core.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores entities in a local SQLite database
 */
public class SQLitePersistence extends Persistence {
	private final static Logger logger = LogManager.getLogger(SQLitePersistence.class.getName());
	private final static String STR_MASTER_KEY = "MASTERKEY";
	private final static String STR_MASTER_KEY_NONCE = "MASTERKEYNONCE";
	private final static String STR_SALT = "SALT";
	private final static String STR_DATA = "DATA";

	private Connection c;

	/**
	 * Stores entities in a local SQLite database
	 * @param password Password to encrypt data.
	 */
	public SQLitePersistence(char[] password) {
		super();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:qabel-core.sqlite");
			createTables();
			init(password);
		} catch (SQLException e) {
			logger.fatal("Cannot connect to SQLite DB!", e);
		} catch (ClassNotFoundException e) {
			logger.fatal("Cannot load JDBC class!", e);
		}
	}

	@Override
	byte[] getSalt(boolean forceNewSalt) {
		byte[] salt = null;

		if (forceNewSalt) {
			deleteConfigValue(STR_SALT);
		} else {
			salt = getConfigValue(STR_SALT);
		}

		if (salt == null) {
			salt = cryptoutils.getRandomBytes(SALT_SIZE_BYTE);
			setConfigValue(STR_SALT, salt);
		}
		return salt;
	}

	@Override
	KeyParameter getMasterKey(KeyParameter encryptionKey) {
		byte[] masterKey = null;
		byte[] masterKeyNonce = getConfigValue(STR_MASTER_KEY_NONCE);

		if (masterKeyNonce != null) {
			try {
				masterKey = cryptoutils.decrypt(encryptionKey, masterKeyNonce, getConfigValue(STR_MASTER_KEY), null);
			} catch (InvalidCipherTextException e) {
				logger.error("Cannot decrypt master key!", e);
			}
		}

		if (masterKey == null) {
			masterKey = cryptoutils.getRandomBytes(AES_KEY_SIZE_BYTE);
			masterKeyNonce = cryptoutils.getRandomBytes(NONCE_SIZE_BYTE);

			if (!setConfigValue(STR_MASTER_KEY_NONCE, masterKeyNonce)) {
				logger.error("Cannot insert master key nonce into database!");
				return null;
			}

			try {
				if (!setConfigValue(STR_MASTER_KEY, cryptoutils.encrypt(encryptionKey, masterKeyNonce, masterKey, null))) {
					logger.error("Cannot insert master key into database!");
					return null;
				}
			} catch (InvalidCipherTextException e) {
				logger.error("Cannot encrypt master key!", e);
				return null;
			}
		}
		return new KeyParameter(masterKey);
	}

	@Override
	boolean reEncryptMasterKey(KeyParameter oldKey, KeyParameter newKey) {
		KeyParameter oldMasterKey = getMasterKey(oldKey);
		boolean success = false;
		try {
			c.setAutoCommit(false);

			deleteConfigValue(STR_MASTER_KEY);
			deleteConfigValue(STR_MASTER_KEY_NONCE);

			byte[] masterKeyNonce = cryptoutils.getRandomBytes(NONCE_SIZE_BYTE);
			setConfigValue(STR_MASTER_KEY_NONCE, masterKeyNonce);
			setConfigValue(STR_MASTER_KEY, cryptoutils.encrypt(newKey, masterKeyNonce, oldMasterKey.getKey(), null));

		} catch (SQLException | InvalidCipherTextException e) {
			logger.error("Cannot re-encrypt master key!", e);
			try {
				c.rollback();
				c.setAutoCommit(true);
			} catch (SQLException e1) {
				logger.error("Cannot rollback changes!", e1);
			}
		} finally {
			try {
				c.commit();
				c.setAutoCommit(true);
				success = true;
			} catch (SQLException e) {
				logger.error("Cannot apply changes!", e);
			}
		}
		return success;
	}

	private void createTables(){
		try {
			String sql = "CREATE TABLE IF NOT EXISTS CONFIG " +
					"(ID TEXT PRIMARY KEY NOT NULL," +
					"DATA TEXT NOT NULL)";
			Statement statement = c.createStatement();
			statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot create CONFIG table!", e);
		}
	}

	private byte[] getConfigValue(String name) {
		byte[] value = null;
		try {
			String sql = "SELECT DATA FROM CONFIG WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			if (!rs.isClosed()) {
				value = rs.getBytes(STR_DATA);
			}
			statement.close();
		} catch (SQLException e) {
			logger.info("Cannot select " + name + " from CONFIG database!", e);
		}
		return value;
	}

	private boolean setConfigValue(String name, byte[] data) {
		try {
			String sql = "INSERT INTO CONFIG " +
					" VALUES(?, ?)";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, name);
			statement.setBytes(2, data);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.info("Cannot insert " + name + " into CONFIG database!", e);
			return false;
		}
		return true;
	}

	private boolean deleteConfigValue(String name) {
		try {
			String sql = "DELETE FROM CONFIG WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, name);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.info("Cannot delete " + name + " from CONFIG database!", e);
			return false;
		}
		return true;
	}

	private byte[] getNonce(String id, Serializable object) {
		byte[] nonce = null;
		try {
			String sql = "SELECT NONCE FROM " +
					"\"" + object.getClass().getCanonicalName() + "\"" +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, id);
			ResultSet rs = statement.executeQuery(sql);
			nonce = rs.getBytes("NONCE");
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot get nonce!", e);
		}
		return nonce;
	}

	@Override
	boolean persistEntity(String id, Serializable object) {
		try {
			String sql = "CREATE TABLE IF NOT EXISTS " +
					"\"" + object.getClass().getCanonicalName() + "\"" +
					"(ID TEXT PRIMARY KEY NOT NULL," +
					"NONCE TEXT NOT NULL," +
					"BLOB TEXT NOT NULL)";
			Statement statement = c.createStatement();
			statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot create table!", e);
		}

		try {
			byte[] nonce = cryptoutils.getRandomBytes(NONCE_SIZE_BYTE);
			String sql = "INSERT INTO " +
					"\"" + object.getClass().getCanonicalName() + "\"" +
					" VALUES(?, ?, ?)";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, id);
			statement.setBytes(2, nonce);
			statement.setBytes(3, serialize(id, object, nonce));
			statement.executeUpdate();
			statement.close();
		} catch (SQLException | InvalidKeySpecException | InvalidCipherTextException
				| NoSuchAlgorithmException | IOException e) {
			logger.error("Cannot persist entity!", e);
			return false;
		}
		return true;
	}

	@Override
	boolean updateEntity(String id, Serializable object) {
		try {
			String sql = "UPDATE " +
					"\"" + object.getClass().getCanonicalName() + "\"" +
					"SET BLOB = ? " +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setBytes(1, serialize(id, object, getNonce(id, object)));
			statement.setString(2, id);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException | IOException | InvalidKeySpecException
				| InvalidCipherTextException | NoSuchAlgorithmException e) {
			logger.error("Cannot update entity!", e);
			return false;
		}
		return true;
	}

	@Override
	boolean removeEntity(String id, Class cls) {
		try {
			String sql = "DELETE FROM " +
					"\"" + cls.getCanonicalName() + "\"" +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, id);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot remove entity!", e);
			return false;
		}
		return true;
	}

	@Override
	Object getEntity(String id, Class cls) {
		Object object = null;
		try {
			String sql = "SELECT BLOB, NONCE FROM " +
					"\"" + cls.getCanonicalName() + "\"" +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, id);
			ResultSet rs = statement.executeQuery(sql);
			object = deserialize(id, rs.getBytes("BLOB"), rs.getBytes("NONCE"));
		} catch (SQLException | InvalidCipherTextException
				| IOException | ClassNotFoundException e) {
			logger.error("Cannot get entity!", e);
		}
		return object;
	}

	@Override
	List<Object> getEntities(Class cls) {
		List<Object> objects = new ArrayList<>();
		try {
			String sql = "SELECT ID, BLOB, NONCE FROM " +
					"\"" + cls.getCanonicalName() + "\"";
			Statement statement = c.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				objects.add(deserialize(new String(rs.getBytes("ID")), rs.getBytes("BLOB"), rs.getBytes("NONCE")));
			}
		} catch (SQLException | InvalidCipherTextException
				| ClassNotFoundException | IOException e) {
			logger.error("Cannot get entities!", e);
		}
		return objects;
	}

	@Override
	boolean dropTable(Class cls) {
		try {
			String sql = "DROP TABLE " +
					"\"" + cls.getCanonicalName() + "\"";
			Statement statement = c.createStatement();
			statement.execute(sql);
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot drop table!" + e);
			return false;
		}
		return true;
	}
}
