package de.qabel.box.storage;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DeleteOnCloseFileInputStreamTest {
    private File file;

    @Before
    public void setUp() throws Exception {
        file = File.createTempFile("test", "file");
        file.createNewFile();
        Files.write(file.toPath(), "content".getBytes());
    }

    @Test
    public void deletesFileOnClose() throws Exception {
        new DeleteOnCloseFileInputStream(file).close();
        assertFalse("file was not deleted", file.exists());
    }

    @Test
    public void deletesFileByNameOnClose() throws Exception {
        new DeleteOnCloseFileInputStream(file.getAbsolutePath()).close();
        assertFalse("file was not deleted", file.exists());
    }

    @Test
    public void canBeConsumedAsNormal() throws Exception {
        String content;
        try (InputStream in = new DeleteOnCloseFileInputStream(file)) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer);
            content = writer.toString();
        }
        assertEquals("content", content);
        assertFalse("file was not deleted", file.exists());
    }
}
