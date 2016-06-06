package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageCorruptMetadata;
import de.qabel.box.storage.exceptions.QblStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractMetadata {
    public static final int TYPE_NONE = -1;
    protected static final Logger logger = LoggerFactory.getLogger(DirectoryMetadata.class);
    protected static final String JDBC_PREFIX = "jdbc:sqlite:";
    protected final Connection connection;
    protected File tempDir;
    File path;

    public AbstractMetadata(Connection connection, File path) {
        this.connection = connection;
        this.path = path;
    }

    /**
     * Path of the metadata file on the local filesystem
     */
    public File getPath() {
        return path;
    }

    /**
     * Writable temporary directory which is used for encryption and decryption
     */
    public File getTempDir() {
        return tempDir;
    }

    Integer getSpecVersion() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                "SELECT version FROM spec_version")) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new QblStorageCorruptMetadata("No version found!");
                }
            }
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    protected void initDatabase() throws SQLException, QblStorageException {
        for (String q : getInitSql()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(q);
            }
        }
    }

    protected abstract String[] getInitSql();
}
