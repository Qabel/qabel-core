package de.qabel.core.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.*;
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
	private final static String JDBC_CLASS_NAME = "org.sqlite.JDBC";
	private final static String JDBC_PREFIX = "jdbc:sqlite:";
	private final static String DEFAULT_DB_NAME = "qabel-core.sqlite";

	private Connection c;

	/**
	 * Stores entities in a local SQLite database
	 */
	public SQLitePersistence() {
		super(DEFAULT_DB_NAME);
	}

	/**
	 * Stores entities in a local SQLite database
	 * @param dbName Database file name.
	 */
	public SQLitePersistence(String dbName) {
		super(dbName);
	}

	@Override
	boolean connect(String dbName) {
		try {
			Class.forName(JDBC_CLASS_NAME);
			c = DriverManager.getConnection(JDBC_PREFIX + dbName);
			createTables();
		} catch (SQLException e) {
			logger.fatal("Cannot connect to SQLite DB!", e);
			return false;
		} catch (ClassNotFoundException e) {
			logger.fatal("Cannot load JDBC class!", e);
			return false;
		}
		return true;
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
		if (encryptionKey == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
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
		if (oldKey == null || newKey == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
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
		if (name == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Name cannot be empty!");
		}
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
		if (name == null || data == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Name cannot be empty!");
		}
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
		if (name == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Name cannot be empty!");
		}
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

	private byte[] getNonce(String id, Class cls) {
		if (id == null || cls == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		byte[] nonce = null;
		try {
			String sql = "SELECT NONCE FROM " +
					"\"" + cls.getCanonicalName() + "\"" +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, id);
			ResultSet rs = statement.executeQuery();
			nonce = rs.getBytes("NONCE");
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot get nonce!", e);
		}
		return nonce;
	}

	@Override
	public boolean persistEntity(String id, Serializable object) {
		if (id == null || object == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		try {
			String sql = "CREATE TABLE IF NOT EXISTS " +
					"\"" + object.getClass().getCanonicalName() + "\"" +
					"(ID TEXT PRIMARY KEY NOT NULL," +
					"NONCE TEXT NOT NULL," +
					"BLOB BLOB NOT NULL)";
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
		} catch (SQLException | IllegalArgumentException e) {
			logger.error("Cannot persist entity!", e);
			throw new IllegalArgumentException("Cannot persist entity!");
		}
		return true;
	}

	@Override
	public boolean updateEntity(String id, Serializable object) {
		if (id == null || object == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		try {
			String sql = "UPDATE " +
					"\"" + object.getClass().getCanonicalName() + "\"" +
					"SET BLOB = ? " +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setBytes(1, serialize(id, object, getNonce(id, object.getClass())));
			statement.setString(2, id);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.error("Cannot update entity!", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean removeEntity(String id, Class cls) {
		if (id == null || cls == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
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
	public Object getEntity(String id, Class cls) {
		if (id == null || cls == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (id.length() == 0) {
			throw new IllegalArgumentException("ID cannot be empty!");
		}
		Object object = null;
		try {
			String sql = "SELECT BLOB, NONCE FROM " +
					"\"" + cls.getCanonicalName() + "\"" +
					" WHERE ID = ?";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, id);
			ResultSet rs = statement.executeQuery();
			object = deserialize(id, rs.getBytes("BLOB"), rs.getBytes("NONCE"));
		} catch (SQLException | IllegalArgumentException e) {
			logger.error("Cannot get entity!", e);
		}
		return object;
	}

	@Override
	public List<Object> getEntities(Class cls) {
		if (cls == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		List<Object> objects = new ArrayList<>();
		try {
			String sql = "SELECT ID, BLOB, NONCE FROM " +
					"\"" + cls.getCanonicalName() + "\"";
			Statement statement = c.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				objects.add(deserialize(new String(rs.getBytes("ID")), rs.getBytes("BLOB"), rs.getBytes("NONCE")));
			}
		} catch (SQLException | IllegalArgumentException e) {
			logger.error("Cannot get entities!", e);
		}
		return objects;
	}

	@Override
	public boolean dropTable(Class cls) {
		if (cls == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
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
