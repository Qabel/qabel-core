package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class DirectoryMetadata extends AbstractMetadata {
    public static final long DEFAULT_SIZE = 56320L;
    private final String fileName;
    byte[] deviceId;
    private String root;

    private static final int TYPE_FILE = 0;
    private static final int TYPE_FOLDER = 1;
    private static final int TYPE_EXTERNAL = 2;

    private static final String[] initSql = {
        "CREATE TABLE meta (" +
            " name VARCHAR(24) PRIMARY KEY," +
            " value TEXT )",
        "CREATE TABLE spec_version (" +
            " version INTEGER PRIMARY KEY )",
        "CREATE TABLE version (" +
            " id INTEGER PRIMARY KEY," +
            " version BLOB NOT NULL," +
            " time LONG NOT NULL )",
        "CREATE TABLE shares (" +
            " ref VARCHAR(255)NOT NULL," +
            " recipient BLOB NOT NULL," +
            " type INTEGER NOT NULL )",
        "CREATE UNIQUE INDEX uniqueShares ON shares(ref, recipient, type)",
        "CREATE TABLE files (" +
            " prefix VARCHAR(255)NOT NULL," +
            " block VARCHAR(255)NOT NULL," +
            " name VARCHAR(255)NOT NULL PRIMARY KEY," +
            " size LONG NOT NULL," +
            " mtime LONG NOT NULL," +
            " key BLOB NOT NULL," +
            " meta VARCAHR(255)," +
            " metakey BLOB)",
        "CREATE TABLE folders (" +
            " ref VARCHAR(255)NOT NULL," +
            " name VARCHAR(255)NOT NULL PRIMARY KEY," +
            " key BLOB NOT NULL )",
        "CREATE TABLE externals (" +
            " is_folder BOOLEAN NOT NULL," +
            " owner BLOB NOT NULL," +
            " name VARCHAR(255)NOT NULL PRIMARY KEY," +
            " key BLOB NOT NULL," +
            " url TEXT NOT NULL )",
        "INSERT INTO spec_version (version) VALUES(0)"
    };

    public DirectoryMetadata(Connection connection, String root, byte[] deviceId,
                             File path, String fileName, File tempDir) {
        this(connection, deviceId, path, fileName, tempDir);
        this.root = root;
    }

    public DirectoryMetadata(Connection connection, byte[] deviceId, File path, String fileName,
                             File tempDir) {
        super(connection, path);
        this.deviceId = deviceId;
        this.fileName = fileName;
        this.tempDir = tempDir;
    }

    /**
     * Create a new database and init it with an sql schema and a metadata
     *
     * @param root     path to the metadata file
     * @param deviceId 16 random bytes that identify the current device
     * @param tempDir  writable temp directory
     */
    public static DirectoryMetadata newDatabase(String root, byte[] deviceId, File tempDir) throws QblStorageException {
        File path;
        try {
            path = File.createTempFile("dir", "db5", tempDir);
            path.deleteOnExit();
        } catch (IOException e) {
            throw new QblStorageIOFailure(e);
        }
        DirectoryMetadata dm = openDatabase(path, deviceId, UUID.randomUUID().toString(), tempDir);
        try {
            dm.initDatabase();
            dm.setRoot(root);
        } catch (SQLException e) {
            throw new QblStorageCorruptMetadata(e);
        }
        return dm;
    }

    /**
     * Open an existing database from a decrypted file
     *
     * @param path     writable location of the metadata file
     * @param deviceId 16 random bytes that identify the current device
     * @param fileName name of the file on the storage backend
     * @param tempDir  writable temp directory
     */
    public static DirectoryMetadata openDatabase(File path, byte[] deviceId, String fileName, File tempDir) throws QblStorageException {
        Connection connection;
        try {
            connection = DriverManager.getConnection(JDBC_PREFIX + path.getAbsolutePath());
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=MEMORY");
            }
        } catch (SQLException e) {
            throw new QblStorageCorruptMetadata(e);
        }
        return new DirectoryMetadata(connection, deviceId, path, fileName, tempDir);
    }

    /**
     * Name of the file on the storage backend
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    protected void initDatabase() throws SQLException, QblStorageException {
        super.initDatabase();
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO version (version, time) VALUES (?, ?)")) {
            statement.setBytes(1, initVersion());
            statement.setLong(2, System.currentTimeMillis());
            statement.executeUpdate();
        }
        setLastChangedBy();
        // only set root if this actually has a root attribute
        // (only index metadata files have it)
        if (root != null) {
            setRoot(root);
        }
    }

    @Override
    protected String[] getInitSql() {
        return initSql;
    }

    private void setRoot(String root) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT OR REPLACE INTO meta (name, value) VALUES ('root', ?)")) {
            statement.setString(1, root);
            statement.executeUpdate();
        }
    }

    String getRoot() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                "SELECT value FROM meta WHERE name='root'")) {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    throw new QblStorageNotFound("No root found!");
                }
            }
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    void setLastChangedBy() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT OR REPLACE INTO meta (name, value) VALUES ('last_change_by', ?)")) {
            String x = new String(Hex.encodeHex(deviceId));
            statement.setString(1, x);
            statement.executeUpdate();
        }

    }

    public byte[] getLastChangedBy() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                "SELECT value FROM meta WHERE name='last_change_by'")) {
                if (rs.next()) {
                    String lastChanged = rs.getString(1);
                    return Hex.decodeHex(lastChanged.toCharArray());
                } else {
                    throw new QblStorageCorruptMetadata("No version found!");
                }
            } catch (DecoderException e) {
                throw new QblStorageCorruptMetadata(e);
            }
        } catch (SQLException e) {
            throw new QblStorageCorruptMetadata(e);
        }
    }

    private byte[] initVersion() throws QblStorageException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new QblStorageDecryptionFailed(e);
        }
        md.update(new byte[]{0, 0});
        md.update(deviceId);
        return md.digest();
    }

    public byte[] getVersion() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                "SELECT version FROM version ORDER BY id DESC LIMIT 1")) {
                if (rs.next()) {
                    return rs.getBytes(1);
                } else {
                    throw new QblStorageCorruptMetadata("No version found!");
                }
            }
        } catch (SQLException e) {
            throw new QblStorageCorruptMetadata(e);
        }
    }

    public void commit() throws QblStorageException {
        byte[] oldVersion = getVersion();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new QblStorageException(e);
        }
        md.update(new byte[]{0, 1});
        md.update(oldVersion);
        md.update(deviceId);
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO version (version, time) VALUES (?, ?)")) {
            statement.setBytes(1, md.digest());
            statement.setLong(2, System.currentTimeMillis());
            if (statement.executeUpdate() != 1) {
                throw new QblStorageException("Could not update version!");
            }
            setLastChangedBy();
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }


    public List<BoxFile> listFiles() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                "SELECT prefix, block, name, size, mtime, key, meta, metakey FROM files")) {
                List<BoxFile> files = new ArrayList<>();
                while (rs.next()) {
                    int i = 0;
                    files.add(new BoxFile(
                        rs.getString(++i),
                        rs.getString(++i),
                        rs.getString(++i),
                        rs.getLong(++i),
                        rs.getLong(++i) * 1000,
                        rs.getBytes(++i),
                        rs.getString(++i),
                        rs.getBytes(++i)
                    ));
                }
                return files;
            }
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    public void insertFile(BoxFile file) throws QblStorageException {
        int type = isA(file.getName());
        if (type != TYPE_NONE) {
            throw new QblStorageNameConflict(file.getName());
        }
        try {
            PreparedStatement st = connection.prepareStatement(
                "INSERT INTO files (prefix, block, name, size, mtime, key, meta, metakey) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
            int i = 0;

            st.setString(++i, file.getPrefix());
            st.setString(++i, file.getBlock());
            st.setString(++i, file.getName());
            st.setLong(++i, file.getSize());
            st.setLong(++i, file.getMtime() / 1000);
            st.setBytes(++i, file.getKey());
            st.setString(++i, file.getMeta());
            st.setBytes(++i, file.getMetakey());
            if (st.executeUpdate() != 1) {
                throw new QblStorageException("Failed to insert file");
            }

        } catch (SQLException e) {
            logger.error("Could not insert file " + file.getName());
            throw new QblStorageException(e);
        }
    }

    public void deleteFile(BoxFile file) throws QblStorageException {
        try {
            PreparedStatement st = connection.prepareStatement(
                "DELETE FROM files WHERE name=?");
            st.setString(1, file.getName());
            if (st.executeUpdate() != 1) {
                throw new QblStorageException("Failed to delete file: Not found");
            }

        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    public void insertFolder(BoxFolder folder) throws QblStorageException {
        int type = isA(folder.getName());
        if (type != TYPE_NONE) {
            throw new QblStorageNameConflict(folder.getName());
        }
        executeStatement(() -> {
            PreparedStatement st = connection.prepareStatement(
                "INSERT INTO folders (ref, name, key) VALUES(?, ?, ?)");
            st.setString(1, folder.getRef());
            st.setString(2, folder.getName());
            st.setBytes(3, folder.getKey());
            return st;
        });
    }

    public void deleteFolder(BoxFolder folder) throws QblStorageException {
        executeStatement(() -> {
            PreparedStatement st = connection.prepareStatement(
                "DELETE FROM folders WHERE name=?");
            st.setString(1, folder.getName());
            return st;
        });
    }

    public List<BoxFolder> listFolders() throws QblStorageException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(
                "SELECT ref, name, key FROM folders")) {
                List<BoxFolder> folders = new ArrayList<>();
                while (rs.next()) {
                    folders.add(new BoxFolder(rs.getString(1), rs.getString(2), rs.getBytes(3)));
                }
                return folders;
            }
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    public void insertShare(BoxShare share) throws QblStorageException {
        executeStatement(() -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO shares (ref, recipient, type) VALUES (?, ?, ?)"
            );
            statement.setString(1, share.getRef());
            statement.setString(2, share.getRecipient());
            statement.setString(3, share.getType());
            return statement;
        });
    }

    private void executeStatement(Callable<PreparedStatement> statementCallable) throws QblStorageException {
        try {
            PreparedStatement statement = statementCallable.call();
            if (statement.executeUpdate() != 1) {
                throw new QblStorageException("Failed to execute statement");
            }
        } catch (Exception e) {
            throw new QblStorageException(e);
        }
    }

    public void deleteShare(BoxShare share) throws QblStorageException {
        executeStatement(() -> {
            PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM shares WHERE ref = ? AND recipient = ? AND type = ?"
            );
            statement.setString(1, share.getRef());
            statement.setString(2, share.getRecipient());
            statement.setString(3, share.getType());
            return statement;
        });
    }

    public List<BoxShare> listShares() throws QblStorageException {
        List<BoxShare> shares = new LinkedList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT ref, recipient, type FROM shares"
            );
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                shares.add(new BoxShare(rs.getString(1), rs.getString(2), rs.getString(3)));
            }
            return shares;
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    void insertExternal(BoxExternalReference external) throws QblStorageException {
        int type = isA(external.name);
        if (type != TYPE_NONE) {
            throw new QblStorageNameConflict(external.name);
        }
        try {
            PreparedStatement st = connection.prepareStatement(
                "INSERT INTO externals (is_folder, url, name, owner, key) VALUES(?, ?, ?, ?, ?)");
            st.setBoolean(1, external.isFolder);
            st.setString(2, external.url);
            st.setString(3, external.name);
            st.setBytes(4, external.owner.getKey());
            st.setBytes(5, external.key);
            if (st.executeUpdate() != 1) {
                throw new QblStorageException("Failed to insert external");
            }

        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    void deleteExternal(BoxExternalReference external) throws QblStorageException {
        executeStatement(() -> {
            PreparedStatement st = connection.prepareStatement(
                "DELETE FROM externals WHERE name=?");
            st.setString(1, external.name);
            return st;
        });
    }

    List<BoxExternal> listExternals() throws QblStorageException {
        return new LinkedList<>();
    }

    public BoxFile getFile(String name) throws QblStorageException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT prefix, block, name, size, mtime, key, meta, metakey FROM files WHERE name=?")) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int i = 0;
                    return new BoxFile(
                        rs.getString(++i),
                        rs.getString(++i),
                        rs.getString(++i),
                        rs.getLong(++i),
                        rs.getLong(++i) * 1000,
                        rs.getBytes(++i),
                        rs.getString(++i),
                        rs.getBytes(++i)
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    public BoxFolder getFolder(String name) throws QblStorageException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT ref, name, key FROM folders WHERE name=?"
        )) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new BoxFolder(rs.getString(1), rs.getString(2), rs.getBytes(3));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new QblStorageException(e);
        }
    }

    public boolean hasFile(String name) throws QblStorageException {
        return getFile(name) != null;
    }

    public boolean hasFolder(String name) throws QblStorageException {
        return getFolder(name) != null;
    }

    int isA(String name) throws QblStorageException {
        String[] types = {"files", "folders", "externals"};
        for (int type = 0; type < 3; type++) {
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name FROM " + types[type] + " WHERE name=?")) {
                statement.setString(1, name);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return type;
                    }
                }
            } catch (SQLException e) {
                throw new QblStorageException(e);
            }
        }
        return TYPE_NONE;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (getPath().exists()) {
            getPath().delete();
        }
    }

    public byte[] getDeviceId() {
        return deviceId;
    }
}
