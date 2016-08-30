package de.qabel.box.storage;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BoxVolumeLocalTest extends BoxVolumeTest {

    private Path tempFolder;
    private LocalReadBackend readBackend;

    @Override
    protected StorageReadBackend getReadBackend() {
        return readBackend;
    }

    @Override
    public void setUpVolume() throws IOException {
        tempFolder = Files.createTempDirectory("");
        prefix = "";

        readBackend = new LocalReadBackend(tempFolder);
        volume = new BoxVolumeImpl(readBackend,
                new LocalWriteBackend(tempFolder),
                keyPair, deviceID, new File(System.getProperty("java.io.tmpdir")), "");
        volume2 = new BoxVolumeImpl(new LocalReadBackend(tempFolder),
                new LocalWriteBackend(tempFolder),
                keyPair, deviceID2, new File(System.getProperty("java.io.tmpdir")), "");
    }

    @Override
    protected void cleanVolume() throws IOException {
        FileUtils.deleteDirectory(tempFolder.toFile());
    }

    @Test
    public void rootRefNotChanged() throws Exception {
        assertThat(volume.getRootRef(), equalTo("300c9c96-03b9-2a4b-39ed-3958bf924011"));
    }
}
