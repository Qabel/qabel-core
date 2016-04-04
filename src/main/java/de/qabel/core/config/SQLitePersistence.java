package de.qabel.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores entities in a local SQLite database
 */
public class SQLitePersistence extends Persistence<String> {
    private final static Logger logger = LoggerFactory.getLogger(SQLitePersistence.class.getName());
    private final static String STR_DATA = "DATA";
    private final static String JDBC_CLASS_NAME = "org.sqlite.JDBC";
    private final static String JDBC_PREFIX = "jdbc:sqlite:";

    private Connection c;

    public SQLitePersistence(String database) {
        connect(database);
    }

    @Override
    protected boolean connect(String database) {
        try {
            Class.forName(JDBC_CLASS_NAME);
            c = DriverManager.getConnection(JDBC_PREFIX + database);
            createTables();
        } catch (SQLException e) {
            logger.error("Cannot connect to SQLite DB!", e);
            return false;
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load JDBC class!", e);
            return false;
        }
        return true;
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS CONFIG " +
            "(ID TEXT PRIMARY KEY NOT NULL," +
            "DATA TEXT NOT NULL)";
        try (Statement statement = c.createStatement()) {
            statement.executeUpdate(sql);
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
        String sql = "SELECT DATA FROM CONFIG WHERE ID = ?";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.isClosed()) {
                    value = rs.getBytes(STR_DATA);
                }
            }
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
        String sql = "INSERT INTO CONFIG " +
            " VALUES(?, ?)";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setBytes(2, data);
            statement.executeUpdate();
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
        String sql = "DELETE FROM CONFIG WHERE ID = ?";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
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
        String sql = "SELECT NONCE FROM " +
            "\"" + cls.getCanonicalName() + "\"" +
            " WHERE ID = ?";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                nonce = rs.getBytes("NONCE");
            }
        } catch (SQLException e) {
            logger.error("Cannot get nonce!", e);
        }
        return nonce;
    }

    @Override
    public boolean persistEntity(Persistable object) {
        if (object == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        String sql = "CREATE TABLE IF NOT EXISTS " +
            "\"" + object.getClass().getCanonicalName() + "\"" +
            "(ID TEXT PRIMARY KEY NOT NULL," +
            "BLOB BLOB NOT NULL)";
        try (Statement statement = c.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error("Cannot create table!", e);
        }

        sql = "INSERT INTO " +
            "\"" + object.getClass().getCanonicalName() + "\"" +
            " VALUES(?, ?)";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, object.getPersistenceID());
            statement.setBytes(2, serialize(object.getPersistenceID(), object));
            statement.executeUpdate();
        } catch (SQLException | IllegalArgumentException e) {
            logger.error("Cannot persist or already persisted entity!", e);
            throw new IllegalArgumentException("Cannot persist or already persisted entity!");
        }
        return true;
    }

    @Override
    public boolean updateEntity(Persistable object) {
        if (object == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        String sql = "UPDATE " +
            "\"" + object.getClass().getCanonicalName() + "\"" +
            "SET BLOB = ? " +
            " WHERE ID = ?";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setBytes(1, serialize(object.getPersistenceID(), object));
            statement.setString(2, object.getPersistenceID());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Cannot update entity!", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateOrPersistEntity(Persistable object) {
        if (getEntity(object.getPersistenceID(), object.getClass()) == null) {
            return persistEntity(object);
        } else {
            return updateEntity(object);
        }
    }

    @Override
    public boolean removeEntity(String id, Class cls) {
        if (id == null || cls == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        if (id.length() == 0) {
            throw new IllegalArgumentException("ID cannot be empty!");
        }
        String sql = "DELETE FROM " +
            "\"" + cls.getCanonicalName() + "\"" +
            " WHERE ID = ?";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Cannot remove entity!", e);
            return false;
        }
        return true;
    }

    @Override
    public <U extends Persistable> U getEntity(String id, Class<? extends U> cls) {
        if (id == null || cls == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        if (id.length() == 0) {
            throw new IllegalArgumentException("ID cannot be empty!");
        }
        U object = null;
        String sql = "SELECT BLOB FROM " +
            "\"" + cls.getCanonicalName() + "\"" +
            " WHERE ID = ?";
        try (PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                object = (U) deserialize(id, rs.getBytes("BLOB"));
            }
        } catch (SQLException | IllegalArgumentException e) {
            logger.error("Cannot get entity!", e);
        }
        return object;
    }

    @Override
    public <U extends Persistable> List<U> getEntities(Class<? extends U> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        List<U> objects = new ArrayList<>();
        String sql = "SELECT ID, BLOB FROM " +
            "\"" + cls.getCanonicalName() + "\"";
        try (Statement statement = c.createStatement()) {
            try (ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    objects.add((U) deserialize(new String(rs.getBytes("ID")), rs.getBytes("BLOB")));
                }
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
        String sql = "DROP TABLE " +
            "\"" + cls.getCanonicalName() + "\"";
        try (Statement statement = c.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error("Cannot drop table!" + e);
            return false;
        }
        return true;
    }
}
