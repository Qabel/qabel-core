package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class LocalBackendTest {

    private byte[] bytes;
    private String testFile;
    private StorageReadBackend readBackend;
    private StorageWriteBackend writeBackend;
    private Path temp;

    @Before
    public void setupTestBackend() throws IOException {
        temp = Files.createTempDirectory(null);
        Path tempFile = Files.createTempFile(temp, null, null);
        bytes = new byte[]{1, 2, 3, 4};
        Files.write(tempFile, bytes);
        readBackend = new LocalReadBackend(temp);
        testFile = tempFile.getFileName().toString();
        writeBackend = new LocalWriteBackend(temp);

    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(temp.toFile());
    }

    @Test
    public void testReadTempFile() throws QblStorageException, IOException {
        assertArrayEquals(bytes, IOUtils.toByteArray(readBackend.download(testFile).getInputStream()));
    }

    @Test
    public void testWriteTempFile() throws QblStorageException, IOException {
        byte[] newBytes = bytes.clone();
        newBytes[0] = 0;
        writeBackend.upload(testFile, new ByteArrayInputStream(newBytes));
        assertArrayEquals(newBytes, IOUtils.toByteArray(readBackend.download(testFile).getInputStream()));
        writeBackend.delete(testFile);
        try {
            readBackend.download(testFile);
            fail("Read should have failed, file is deleted");
        } catch (QblStorageException e) {

        }

    }
}
