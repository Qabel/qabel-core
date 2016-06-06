package de.qabel.box.storage;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.box.storage.exceptions.QblStorageException;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class FileMetadata extends AbstractMetadata {
    private static final String[] initSql = {
        "CREATE TABLE spec_version (" +
            " version INTEGER PRIMARY KEY )",
        "CREATE TABLE file (" +
            " owner BLOB NOT NULL," +
            " prefix VARCHAR(255) NOT NULL," +
            " block VARCHAR(255) NOT NULL," +
            " name VARCHAR(255) NULL PRIMARY KEY," +
            " size LONG NOT NULL," +
            " mtime LONG NOT NULL," +
            " key BLOB NOT NULL )",
        "INSERT INTO spec_version (version) VALUES(0)"
    };

    public static FileMetadata openExisting(File path) {
        try {
            Connection connection = DriverManager.getConnection(JDBC_PREFIX + path.getAbsolutePath());
            connection.setAutoCommit(true);
            return new FileMetadata(connection, path);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot open database!", e);
        }
    }

    public static FileMetadata openNew(QblECPublicKey owner, BoxFile boxFile, File tmpDir) throws QblStorageException {
        try {
            File path = File.createTempFile("dir", "db6", tmpDir);

            Connection connection = DriverManager.getConnection(JDBC_PREFIX + path.getAbsolutePath());
            connection.setAutoCommit(true);
            return new FileMetadata(connection, path, owner, boxFile);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot open database!", e);
        } catch (IOException e) {
            throw new QblStorageException(e);
        }

    }

    public FileMetadata(Connection connection, File path) {
        super(connection, path);
    }

    public FileMetadata(Connection connection, File path, QblECPublicKey owner, BoxFile boxFile) throws QblStorageException, SQLException {
        this(connection, path);

        initDatabase();
        insertFile(owner, boxFile);
    }

    @Override
    protected String[] getInitSql() {
        return initSql;
    }

    private void insertFile(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO file (owner, prefix, block, name, size, mtime, key) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            statement.setBytes(++i, owner.getKey());
            statement.setString(++i, boxFile.getPrefix());
            statement.setString(++i, boxFile.getBlock());
            statement.setString(++i, boxFile.getName());
            statement.setLong(++i, boxFile.getSize());
            statement.setLong(++i, boxFile.getMtime());
            statement.setBytes(++i, boxFile.getKey());
            if (statement.executeUpdate() != 1) {
                throw new QblStorageException("Failed to insert file");
            }

        } catch (SQLException e) {
            logger.error("Could not insert file " + boxFile.getName());
            throw new QblStorageException(e);
        }
    }

    public BoxExternalFile getFile() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT owner, prefix, block, name, size, mtime, key FROM file LIMIT 1");
            if (rs.next()) {
                int i = 0;
                byte[] ownerKey = rs.getBytes(++i);
                String prefix = rs.getString(++i);
                String block = rs.getString(++i);
                String name = rs.getString(++i);
                long size = rs.getLong(++i);
                long mtime = rs.getLong(++i);
                byte[] key = rs.getBytes(++i);
                return new BoxExternalFile(new QblECPublicKey(ownerKey), prefix, block, name, size, mtime, key);
            }
            return null;
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }
}
