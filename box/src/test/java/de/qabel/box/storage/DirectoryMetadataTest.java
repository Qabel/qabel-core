package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class DirectoryMetadataTest {

    private DirectoryMetadata dm;

    @Before
    public void setUp() throws Exception {
        // device id
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        dm = DirectoryMetadata.Companion.newDatabase("https://localhost", bb.array(),
                new File(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void testInitDatabase() throws QblStorageException {
        byte[] version = dm.getVersion();
        assertThat(dm.listFiles().size(), is(0));
        assertThat(dm.listFolders().size(), is(0));
        assertThat(dm.listExternals().size(), is(0));
        assertThat(dm.listShares().size(), is(0));
        dm.commit();
        assertThat(dm.getVersion(), is(not(equalTo(version))));

    }

    @Test
    public void testFileOperations() throws QblStorageException {
        BoxFile file = new BoxFile("prefix", "block", "name", 0L, 10000L, new byte[]{1, 2,});
        dm.insertFile(file);
        assertThat(dm.listFiles().size(), is(1));
        assertThat(file, equalTo(dm.listFiles().get(0)));
        assertThat(dm.getFile("name"), is(file));
        assertThat(dm.getFile("name").getMtime(), equalTo(10000L));
        dm.deleteFile(file);
        assertThat(dm.listFiles().size(), is(0));
        assertNull(dm.getFile("name"));
    }

    @Test
    public void testFolderOperations() throws QblStorageException {
        BoxFolder folder = new BoxFolder("block", "name", new byte[]{1, 2,});
        dm.insertFolder(folder);
        assertThat(dm.listFolders().size(), is(1));
        assertThat(folder, equalTo(dm.listFolders().get(0)));
        dm.deleteFolder(folder);
        assertThat(dm.listFolders().size(), is(0));
        assertThat(dm.getPath().getAbsolutePath().toString(), startsWith(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void testAddsShare() throws Exception {
        BoxShare share = new BoxShare("ref", "recipient", "READ");
        dm.insertShare(share);
        assertThat(dm.listShares().size(), is(1));
        BoxShare loaded = dm.listShares().get(0);
        assertEquals("ref", loaded.getRef());
        assertEquals("recipient", loaded.getRecipient());
        assertEquals("READ", loaded.getType());
    }

    @Test
    public void testDeletesShares() throws Exception {
        BoxShare readShare = new BoxShare("ref1", "recipient1", "READ");
        BoxShare writeShare = new BoxShare("ref1", "recipient1", "WRITE");
        BoxShare otherShare = new BoxShare("something", "else", "WRITE");

        dm.insertShare(readShare);
        dm.insertShare(writeShare);
        dm.insertShare(otherShare);

        dm.deleteShare(writeShare);

        assertThat(dm.listShares().size(), is(2));
        BoxShare share0 = dm.listShares().get(0);
        BoxShare share1 = dm.listShares().get(1);
        assertThat(share0.getRef(), equalTo("ref1"));
        assertThat(share0.getType(), equalTo("READ"));
        assertThat(share1.getRef(), equalTo("something"));
    }

    @Test(expected = QblStorageException.class)
    public void givenDuplicateShare_throwsException() throws Exception {
        BoxShare share1 = new BoxShare("a", "b");
        BoxShare share2 = new BoxShare("a", "b");

        dm.insertShare(share1);
        dm.insertShare(share2);
    }

    @Test
    public void testLastChangedBy() throws SQLException, QblStorageException {
        assertThat(dm.getDeviceId(), is(dm.getLastChangedBy()));
        dm.setDeviceId(new byte[]{1, 1});
        dm.setLastChangedBy();
        assertThat(dm.getDeviceId(), is(dm.getLastChangedBy()));
    }

    @Test
    public void testRoot() throws QblStorageException {
        assertThat(dm.getRoot(), startsWith("https://"));
    }

    @Test
    public void testSpecVersion() throws QblStorageException {
        assertThat(dm.getSpecVersion(), is(0));
    }
}
